package com.elasticsearch.cn.elasticsearch.service;

import com.elasticsearch.cn.elasticsearch.bean.House;
import com.elasticsearch.cn.elasticsearch.dao.HouseMapper;
import com.elasticsearch.cn.elasticsearch.form.RentSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 项目名称：elasticsearch
 * 类名称：com.elasticsearch.cn.elasticsearch.service
 * 类描述：
 * 创建人：simonsfan
 * 创建时间：2018/9/25 17:08
 */
@Service
public class HouseServiceImpl implements HouseService {

    @Autowired
    private HouseMapper houseMapper;

    @Override
    public List<House> query(RentSearch rentSearch) {
        return houseMapper.query(rentSearch);
    }

    @Override
    public House getHouseByHouseId(Long houseId) {
        return houseMapper.selectByPrimaryKey(houseId.intValue());
    }
}
