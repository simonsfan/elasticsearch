package com.elasticsearch.cn.elasticsearch.dao;

import com.elasticsearch.cn.elasticsearch.bean.Subway;

public interface SubwayMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Subway record);

    int insertSelective(Subway record);

    Subway selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Subway record);

    int updateByPrimaryKey(Subway record);
}
