package com.elasticsearch.cn.elasticsearch.service.search;

import com.elasticsearch.cn.elasticsearch.bean.House;
import com.elasticsearch.cn.elasticsearch.bean.HouseDetail;
import com.elasticsearch.cn.elasticsearch.bean.HouseTag;
import com.elasticsearch.cn.elasticsearch.service.HouseDetailService;
import com.elasticsearch.cn.elasticsearch.service.HouseService;
import com.elasticsearch.cn.elasticsearch.service.HouseTagService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
import org.elasticsearch.rest.RestStatus;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class SearchServiceImpl implements SearchService {

    private static final Logger logger = LoggerFactory.getLogger(SearchServiceImpl.class);

    private static final String INDEX_NAME = "xunwu";

    private static final String INDEX_TYPE = "house";

    private static final String INDEX_TOPIC = "house_build";

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private TransportClient esClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HouseService houseService;

    @Autowired
    private HouseDetailService houseDetailService;

    @Autowired
    private HouseTagService houseTagService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = INDEX_TOPIC)
    private void handleMessage(String content) {
        try {
            System.out.println("进入消费者队列111111111111…………");
            HouseIndexMessage message = objectMapper.readValue(content, HouseIndexMessage.class);
            System.out.println("进入消费者队列2222222222…………");
            switch (message.getOperation()) {
                case HouseIndexMessage.INDEX:
                    this.createOrUpdateIndex(message);
                    break;
                case HouseIndexMessage.REMOVE:
                    this.removeIndex(message);
                    break;
                default:
                    logger.warn("Not support message content " + content);
                    break;
            }
        } catch (IOException e) {
            logger.error("Cannot parse json for " + content, e);
        }
    }

    /**
     * es+java api请参考https://www.elastic.co/guide/en/elasticsearch/client/java-api/5.6/
     *
     * @param message
     */
    private void createOrUpdateIndex(HouseIndexMessage message) {
        Long houseId = message.getHouseId();
        House house = houseService.getHouseByHouseId(houseId);
        if (house == null) {
            logger.error("index house:{} does not exist", house);
            this.index(houseId, message.getRetry() + 1);
            return;
        }

        HouseIndexTemplate indexTemplate = new HouseIndexTemplate();
        modelMapper.map(house,indexTemplate);

        List<HouseDetail> houseDetailList = houseDetailService.getHouseDetailByHouseId(String.valueOf(houseId));
        if(CollectionUtils.isEmpty(houseDetailList)){
            logger.error("");
            return;
        }
        HouseDetail houseDetail = houseDetailList.get(0);
        modelMapper.map(houseDetail,indexTemplate);

        List<HouseTag> houseTagList = houseTagService.getHouseTagByHouseId(String.valueOf(houseId));
        List<String> tagList = new ArrayList<>();
        for (HouseTag houseTag : houseTagList) {
            tagList.add(houseTag.getName());
        }
        indexTemplate.setTags(tagList);

        //根据house_id查询索引中的数据
        SearchRequestBuilder searchRequestBuilder = this.esClient.prepareSearch(INDEX_NAME).setTypes(INDEX_TYPE).setQuery(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, houseId));

        SearchResponse searchResponse = searchRequestBuilder.get();

        long totalHits = searchResponse.getHits().getTotalHits();

        boolean flag;
        /*判断是否存在：1、不存在，新增  2、存在一条，更新  3、不止一条，删除所有，再新增*/
        if(totalHits == 0){
            flag=   this.create(indexTemplate);
        }else if(totalHits == 1){
            String esId = searchResponse.getHits().getAt(0).getId();
            flag= this.update(esId,indexTemplate);
        }else{
            flag=this.deleteAndCreate(totalHits,indexTemplate);
        }
        if(flag){
            logger.info("Index success with houseid:{}",houseId);
        }
    }

    @Override
    public void index(Long houseId) {
            this.index(houseId,0);
    }

    private void index(Long houseId, int retry) {
        if (retry > HouseIndexMessage.MAX_RETRY) {
            logger.error("Retry index times over 3 for house: " + houseId + " Please check it!");
            return;
        }
        HouseIndexMessage message = new HouseIndexMessage(houseId, HouseIndexMessage.INDEX, retry);
        try {
            ListenableFuture<SendResult<String, String>> send = kafkaTemplate.send(INDEX_TOPIC, objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            logger.error("Json encode error for " + message);
        }
    }

    private void removeIndex(HouseIndexMessage message) {
        DeleteByQueryRequestBuilder requestBuilder = DeleteByQueryAction.INSTANCE.newRequestBuilder(esClient).filter(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, message.getHouseId())).source(INDEX_NAME);
        BulkByScrollResponse bulkByScrollResponse = requestBuilder.get();
        long deleted = bulkByScrollResponse.getDeleted();
        logger.info("houseid={},删除成功",message.getHouseId());
        if (deleted <= 0) {
            logger.error("");
            this.remove(message.getHouseId(),message.getRetry()+1);
            return;
        }
    }

    private void remove(long houseId,int retry) {
        if (retry > HouseIndexMessage.MAX_RETRY) {
            logger.error("Retry remove index times over 3 for house: " + houseId + " Please check it!");
            return;
        }
        HouseIndexMessage message = new HouseIndexMessage(houseId, HouseIndexMessage.REMOVE, retry);
        try {
            ListenableFuture<SendResult<String, String>> send = kafkaTemplate.send(INDEX_TOPIC, objectMapper.writeValueAsString(message));
            boolean done = send.isDone();
            System.out.println("kafka send result="+done);
        } catch (JsonProcessingException e) {
            logger.error("Json encode error for " + message);
        }
    }


    /**
     * 移除房源索引
     *
     * @param houseId
     */
    @Override
    public void remove(Long houseId) {
        this.remove(houseId,0);
    }


    /*新增*/
    private boolean create(HouseIndexTemplate indexTemplate) {
        try {
            IndexResponse indexResponse = this.esClient.prepareIndex(INDEX_NAME, INDEX_TYPE).setSource(objectMapper.writeValueAsBytes(indexTemplate), XContentType.JSON).get();
            logger.info("");
            if (indexResponse.status().equals(RestStatus.CREATED)) {
                return true;
            }
            return false;
        } catch (JsonProcessingException e) {
            logger.error("");
            return false;
        }
    }

    /*更新索引*/
    private boolean update(String esId, HouseIndexTemplate indexTemplate) {
        try {
            UpdateResponse updateResponse = this.esClient.prepareUpdate(INDEX_NAME, INDEX_TYPE, esId).setDoc(objectMapper.writeValueAsBytes(indexTemplate), XContentType.JSON).get();
            logger.info("");
            if (updateResponse.status().equals(RestStatus.OK)) {
                return true;
            }
            return false;
        } catch (JsonProcessingException e) {
            logger.error("");
            return false;
        }
    }

    /*删除索引再新增*/
    private boolean deleteAndCreate(long totolHit, HouseIndexTemplate indexTemplate) {
        DeleteByQueryRequestBuilder requestBuilder = DeleteByQueryAction.INSTANCE.newRequestBuilder(esClient).filter(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, indexTemplate.getHouseId())).source(INDEX_NAME);
        BulkByScrollResponse bulkByScrollResponse = requestBuilder.get();
        long deleted = bulkByScrollResponse.getDeleted();
        logger.info("");
        if (deleted != totolHit) {
            logger.error("need delete {},but {} was deleted!", totolHit, deleted);
            return false;
        }
        return create(indexTemplate);
    }

}
