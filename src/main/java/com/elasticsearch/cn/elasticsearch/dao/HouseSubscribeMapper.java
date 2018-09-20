package com.elasticsearch.cn.elasticsearch.dao;

import com.elasticsearch.cn.elasticsearch.bean.HouseSubscribe;

public interface HouseSubscribeMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(HouseSubscribe record);

    int insertSelective(HouseSubscribe record);

    HouseSubscribe selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(HouseSubscribe record);

    int updateByPrimaryKey(HouseSubscribe record);
}
