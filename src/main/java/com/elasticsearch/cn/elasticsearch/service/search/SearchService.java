package com.elasticsearch.cn.elasticsearch.service.search;

import com.elasticsearch.cn.elasticsearch.form.RentSearch;
import com.elasticsearch.cn.elasticsearch.result.CommonResult;

import java.util.List;

/**
 * 检索接口
 */
public interface SearchService {
    /**
     * 索引目标房源
     * @param houseId
     */
    void index(Long houseId);

    /**
     * 移除房源索引
     * @param houseId
     */
    void remove(Long houseId);

    List<Long> queryByEs(RentSearch rentSearch);

    CommonResult<List<String>> suggest(String prefix);

    Long aggregateDistrictHouse(String cityName,String regionName,String district);

}
