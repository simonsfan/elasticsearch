package com.elasticsearch.cn.elasticsearch.service;

import com.elasticsearch.cn.elasticsearch.bean.House;
import com.elasticsearch.cn.elasticsearch.dao.HouseMapper;
import com.elasticsearch.cn.elasticsearch.form.RentSearch;
import com.elasticsearch.cn.elasticsearch.service.search.SearchService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 项目名称：elasticsearch
 * 创建人：simonsfan
 * 创建时间：2018/9/25
 */
@Service
public class HouseServiceImpl implements HouseService {

    private static final Logger logger = LoggerFactory.getLogger(HouseServiceImpl.class);

    @Autowired
    private HouseMapper houseMapper;

    @Autowired
    private SearchService searchService;

    @Override
    public List<House> query(RentSearch rentSearch) {
        List<House> houseList = new ArrayList<>();
        try {
            if (rentSearch.getKeywords() != null && !rentSearch.getKeywords().isEmpty()) {
                List<Long> houseIdList = searchService.queryByEs(rentSearch);
                if (CollectionUtils.isEmpty(houseIdList)) {
                    return houseList;
                }
                for (Long houseId : houseIdList) {
                    House house = houseMapper.selectByPrimaryKey(houseId.intValue());
                    if (house != null) {
                        houseList.add(house);
                    }
                }
            } else {
                houseList = houseMapper.query(rentSearch);
            }
        } catch (Exception e) {
            logger.error("");
        }
        return houseList;
    }

    @Override
    public House getHouseByHouseId(Long houseId) {
        return houseMapper.selectByPrimaryKey(houseId.intValue());
    }
}
