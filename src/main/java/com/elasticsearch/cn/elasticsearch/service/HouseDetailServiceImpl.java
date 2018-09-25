package com.elasticsearch.cn.elasticsearch.service;

import com.elasticsearch.cn.elasticsearch.bean.HouseDetail;
import com.elasticsearch.cn.elasticsearch.dao.HouseDetailMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 项目名称：elasticsearch
 * 类名称：com.elasticsearch.cn.elasticsearch.service
 * 类描述：
 * 创建人：simonsfan
 * 创建时间：2018/9/25 18:56
 */
@Service
public class HouseDetailServiceImpl implements HouseDetailService {

    @Autowired
    private HouseDetailMapper houseDetailMapper;

    @Override
    public List<HouseDetail> getHouseDetailByHouseId(String houseId) {
        return houseDetailMapper.getHouseDetailByHouseId(houseId);
    }
}
