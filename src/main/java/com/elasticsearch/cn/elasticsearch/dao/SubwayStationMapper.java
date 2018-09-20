package com.elasticsearch.cn.elasticsearch.dao;

import com.elasticsearch.cn.elasticsearch.bean.SubwayStation;

public interface SubwayStationMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SubwayStation record);

    int insertSelective(SubwayStation record);

    SubwayStation selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SubwayStation record);

    int updateByPrimaryKey(SubwayStation record);
}
