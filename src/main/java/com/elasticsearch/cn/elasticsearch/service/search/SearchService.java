package com.elasticsearch.cn.elasticsearch.service.search;

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
}
