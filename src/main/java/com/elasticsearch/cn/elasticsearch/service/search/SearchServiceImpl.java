package com.elasticsearch.cn.elasticsearch.service.search;

import com.elasticsearch.cn.elasticsearch.base.RentValueBlock;
import com.elasticsearch.cn.elasticsearch.bean.House;
import com.elasticsearch.cn.elasticsearch.bean.HouseDetail;
import com.elasticsearch.cn.elasticsearch.bean.HouseTag;
import com.elasticsearch.cn.elasticsearch.form.RentSearch;
import com.elasticsearch.cn.elasticsearch.result.CommonResult;
import com.elasticsearch.cn.elasticsearch.service.HouseDetailService;
import com.elasticsearch.cn.elasticsearch.service.HouseService;
import com.elasticsearch.cn.elasticsearch.service.HouseTagService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeAction;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequestBuilder;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 1、 elastic search + java api请参考：https://www.elastic.co/guide/en/elasticsearch/client/java-api/5.6/
 * 2、 项目使用的分词器-IK请参考：https://github.com/medcl/elasticsearch-analysis-ik/releases
 * 3、 kafka的简单安装与使用请参照：http://kafka.apache.org/quickstart
 * 4、 自动补全功能：search-suggests请参考：
 */
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
            logger.info("=======================进入kafka消费队列==================");
            HouseIndexMessage message = objectMapper.readValue(content, HouseIndexMessage.class);
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


    @Override
    public void index(Long houseId) {
        this.index(houseId, 0);
    }

    private void index(Long houseId, int retry) {
        if (retry > HouseIndexMessage.MAX_RETRY) {
            logger.error("Retry index times over 3 for house: " + houseId + " Please check it!");
            return;
        }
        HouseIndexMessage message = new HouseIndexMessage(houseId, HouseIndexMessage.INDEX, retry);
        try {
            kafkaTemplate.send(INDEX_TOPIC, objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            logger.error("Json encode error for " + message);
        }
    }

    @Override
    public CommonResult<List<String>> suggest(String prefix) {
        CompletionSuggestionBuilder suggestion = SuggestBuilders.completionSuggestion("suggest").prefix(prefix).size(5);

        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion("autocomplete", suggestion);

        SearchRequestBuilder requestBuilder = this.esClient.prepareSearch(INDEX_NAME)
                .setTypes(INDEX_TYPE)
                .suggest(suggestBuilder);
        logger.debug(requestBuilder.toString());

        SearchResponse response = requestBuilder.get();
        Suggest suggest = response.getSuggest();
        if (suggest == null) {
            return CommonResult.success(-1, "data is null", new ArrayList<String>());
        }
        Suggest.Suggestion result = suggest.getSuggestion("autocomplete");

        int maxSuggest = 0;
        Set<String> suggestSet = new HashSet<>();

        for (Object term : result.getEntries()) {
            if (term instanceof CompletionSuggestion.Entry) {
                CompletionSuggestion.Entry item = (CompletionSuggestion.Entry) term;

                if (item.getOptions().isEmpty()) {
                    continue;
                }

                for (CompletionSuggestion.Entry.Option option : item.getOptions()) {
                    String tip = option.getText().string();
                    if (suggestSet.contains(tip)) {
                        continue;
                    }
                    suggestSet.add(tip);
                    maxSuggest++;
                }
            }

            if (maxSuggest > 5) {
                break;
            }
        }
        List<String> suggests = Lists.newArrayList(suggestSet.toArray(new String[]{}));
        return CommonResult.success(200, "success", suggests);
    }

    private boolean updateSuggest(HouseIndexTemplate indexTemplate) {
        AnalyzeRequestBuilder requestBuilder = new AnalyzeRequestBuilder(
                this.esClient, AnalyzeAction.INSTANCE, INDEX_NAME, indexTemplate.getTitle(),
                indexTemplate.getLayoutDesc(), indexTemplate.getRoundService(),
                indexTemplate.getDescription(), indexTemplate.getSubwayLineName(),
                indexTemplate.getSubwayStationName());

        requestBuilder.setAnalyzer("ik_smart");

        AnalyzeResponse response = requestBuilder.get();
        List<AnalyzeResponse.AnalyzeToken> tokens = response.getTokens();
        if (tokens == null) {
            logger.warn("Can not analyze token for house: " + indexTemplate.getHouseId());
            return false;
        }

        List<HouseSuggest> suggests = new ArrayList<>();
        for (AnalyzeResponse.AnalyzeToken token : tokens) {
            // 排序数字类型 & 小于2个字符的分词结果
            if ("<NUM>".equals(token.getType()) || token.getTerm().length() < 2) {
                continue;
            }

            HouseSuggest suggest = new HouseSuggest();
            suggest.setInput(token.getTerm());
            suggests.add(suggest);
        }

        // 定制化小区自动补全
        HouseSuggest suggest = new HouseSuggest();
        suggest.setInput(indexTemplate.getDistrict());
        suggests.add(suggest);

        indexTemplate.setSuggest(suggests);
        return true;
    }

    /**
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
        modelMapper.map(house, indexTemplate);

        List<HouseDetail> houseDetailList = houseDetailService.getHouseDetailByHouseId(String.valueOf(houseId));
        if (CollectionUtils.isEmpty(houseDetailList)) {
            logger.error("");
            return;
        }
        HouseDetail houseDetail = houseDetailList.get(0);
        modelMapper.map(houseDetail, indexTemplate);

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
        if (totalHits == 0) {
            flag = this.create(indexTemplate);
        } else if (totalHits == 1) {
            String esId = searchResponse.getHits().getAt(0).getId();
            flag = this.update(esId, indexTemplate);
        } else {
            flag = this.deleteAndCreate(totalHits, indexTemplate);
        }
        if (flag) {
            logger.info("Index success with houseid:{}", houseId);
        }
    }

    private void removeIndex(HouseIndexMessage message) {
        DeleteByQueryRequestBuilder requestBuilder = DeleteByQueryAction.INSTANCE.newRequestBuilder(esClient).filter(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, message.getHouseId())).source(INDEX_NAME);
        BulkByScrollResponse bulkByScrollResponse = requestBuilder.get();
        long deleted = bulkByScrollResponse.getDeleted();
        logger.info("houseid={},删除成功", message.getHouseId());
        if (deleted <= 0) {
            logger.error("");
            this.remove(message.getHouseId(), message.getRetry() + 1);
            return;
        }
    }

    private void remove(long houseId, int retry) {
        if (retry > HouseIndexMessage.MAX_RETRY) {
            logger.error("Retry remove index times over 3 for house: " + houseId + " Please check it!");
            return;
        }
        HouseIndexMessage message = new HouseIndexMessage(houseId, HouseIndexMessage.REMOVE, retry);
        try {
            ListenableFuture<SendResult<String, String>> send = kafkaTemplate.send(INDEX_TOPIC, objectMapper.writeValueAsString(message));
            boolean done = send.isDone();
            System.out.println("kafka send result=" + done);
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
        this.remove(houseId, 0);
    }

    /**
     * 从ES中查询
     *
     * @param rentSearch
     * @return
     */
    @Override
    public List<Long> queryByEs(RentSearch rentSearch) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //城市名称是必选的，一定得过滤
        boolQueryBuilder.filter(QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME, rentSearch.getCityEnName()));

        //过滤区域
        if (rentSearch.getRegionEnName() != null && !"*".equals(rentSearch.getRegionEnName())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery(HouseIndexKey.REGION_EN_NAME, rentSearch.getRegionEnName()));
        }

        //过滤区域
        RentValueBlock area = RentValueBlock.matchArea(rentSearch.getAreaBlock());
        if (!RentValueBlock.ALL.equals(area)) {
            RangeQueryBuilder areaRangeQueryBuilder = QueryBuilders.rangeQuery(HouseIndexKey.AREA);
            if (area.getMax() > 0) {
                areaRangeQueryBuilder.lte(area.getMax());
            }
            if (area.getMin() < 0) {
                areaRangeQueryBuilder.gte(area.getMin());
            }
            boolQueryBuilder.filter(areaRangeQueryBuilder);
        }

        //过滤价格
        RentValueBlock price = RentValueBlock.matchArea(rentSearch.getPriceBlock());
        if (!RentValueBlock.ALL.equals(price)) {
            RangeQueryBuilder priceRangeQueryBuilder = QueryBuilders.rangeQuery(HouseIndexKey.PRICE);
            if (price.getMax() > 0) {
                priceRangeQueryBuilder.lte(price.getMax());
            }
            if (price.getMin() < 0) {
                priceRangeQueryBuilder.gte(price.getMin());
            }
            boolQueryBuilder.filter(priceRangeQueryBuilder);
        }

        //过滤朝向
        if (rentSearch.getDirection() > 0) {
            boolQueryBuilder.filter(QueryBuilders.termQuery(HouseIndexKey.DIRECTION, rentSearch.getDirection()));
        }

        //过滤租赁方式
        if (rentSearch.getRentWay() > -1) {
            boolQueryBuilder.filter(QueryBuilders.termQuery(HouseIndexKey.RENT_WAY, rentSearch.getRentWay()));
        }

        //关键词分词过滤
        boolQueryBuilder.must(QueryBuilders.multiMatchQuery(
                rentSearch.getKeywords(),
                HouseIndexKey.TITLE,
                HouseIndexKey.TRAFFIC,
                HouseIndexKey.DISTRICT,
                HouseIndexKey.ROUND_SERVICE,
                HouseIndexKey.SUBWAY_LINE_NAME,
                HouseIndexKey.SUBWAY_STATION_NAME
        ));

        SearchRequestBuilder searchRequestBuilder = this.esClient.
                prepareSearch(INDEX_NAME)
                .setTypes(INDEX_TYPE)
                .setQuery(boolQueryBuilder)
                .addSort(HouseSort.getSortKey(rentSearch.getOrderBy()),
                        SortOrder.fromString(rentSearch.getOrderDirection())
                )
                .setFrom(rentSearch.getStart())
                .setSize(rentSearch.getSize())
                .setFetchSource(HouseIndexKey.HOUSE_ID, null);

        logger.info("requestSearchbuilder=" + searchRequestBuilder.toString());
        SearchResponse searchResponse = searchRequestBuilder.get();
        List<Long> houseIdList = new ArrayList<>();
        if (searchResponse.status() != RestStatus.OK) {
            logger.error("");
            return houseIdList;
        }

        for (SearchHit searchHit : searchResponse.getHits()) {
            houseIdList.add(Long.parseLong(String.valueOf(searchHit.getSource().get(HouseIndexKey.HOUSE_ID))));
        }

        return houseIdList;
    }


    /*新增*/
    private boolean create(HouseIndexTemplate indexTemplate) {
        if (!updateSuggest(indexTemplate)) {
            return false;
        }
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
        if (!updateSuggest(indexTemplate)) {
            return false;
        }
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
