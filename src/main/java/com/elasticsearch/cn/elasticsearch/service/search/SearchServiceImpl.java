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
import org.springframework.stereotype.Service;

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

    /**
     * 索引目标房源
     *
     * @param houseId
     */
    @Override
    public void index(Long houseId) {
        House house = houseService.getHouseByHouseId(houseId);
        if (house == null) {
            logger.error("index house:{} does not exist", house);
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

        SearchRequestBuilder searchRequestBuilder = this.esClient.prepareSearch(INDEX_NAME).setTypes(INDEX_TYPE).setQuery(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, houseId));

        SearchResponse searchResponse = searchRequestBuilder.get();

        long totalHits = searchResponse.getHits().getTotalHits();

        boolean flag;
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

    /*创建索引*/
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

    /*删除索引*/
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


    /**
     * 移除房源索引
     *
     * @param houseId
     */
    @Override
    public void remove(Long houseId) {

    }
}
