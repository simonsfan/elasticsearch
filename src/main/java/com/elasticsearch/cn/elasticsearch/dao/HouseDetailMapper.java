package com.elasticsearch.cn.elasticsearch.dao;

import com.elasticsearch.cn.elasticsearch.bean.HouseDetail;

public interface HouseDetailMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(HouseDetail record);

    int insertSelective(HouseDetail record);

    HouseDetail selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(HouseDetail record);

    int updateByPrimaryKey(HouseDetail record);
}
