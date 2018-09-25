package com.elasticsearch.cn.elasticsearch.dao;

import com.elasticsearch.cn.elasticsearch.bean.SupportAddress;

import java.util.List;
import java.util.Map;

public interface SupportAddressMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SupportAddress record);

    int insertSelective(SupportAddress record);

    SupportAddress selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SupportAddress record);

    int updateByPrimaryKey(SupportAddress record);

    List<SupportAddress> findByEnName(Map<String, Object> map);

    List<SupportAddress> findAllByLevelAndBelongTo(Map<String, Object> map);
}
