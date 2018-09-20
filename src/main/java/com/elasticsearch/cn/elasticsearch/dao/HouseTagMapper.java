package com.elasticsearch.cn.elasticsearch.dao;

import com.elasticsearch.cn.elasticsearch.bean.HouseTag;

public interface HouseTagMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(HouseTag record);

    int insertSelective(HouseTag record);

    HouseTag selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(HouseTag record);

    int updateByPrimaryKey(HouseTag record);
}
