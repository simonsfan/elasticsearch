package com.elasticsearch.cn.elasticsearch.service;

import com.elasticsearch.cn.elasticsearch.bean.HouseTag;
import com.elasticsearch.cn.elasticsearch.dao.HouseTagMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 项目名称：elasticsearch
 * 类名称：com.elasticsearch.cn.elasticsearch.service
 * 类描述：
 * 创建人：simonsfan
 * 创建时间：2018/9/25 18:30
 */
@Service
public class HouseTagServiceImpl implements HouseTagService {

    @Autowired
    private HouseTagMapper houseTagMapper;

    @Override
    public List<HouseTag> getHouseTagByHouseId(String houseId) {
        return houseTagMapper.getHouseTagByHouseId(houseId);
    }
}
