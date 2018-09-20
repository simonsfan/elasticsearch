package com.elasticsearch.cn.elasticsearch.dao;

import com.elasticsearch.cn.elasticsearch.bean.SupportAddress;

public interface SupportAddressMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SupportAddress record);

    int insertSelective(SupportAddress record);

    SupportAddress selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SupportAddress record);

    int updateByPrimaryKey(SupportAddress record);
}
