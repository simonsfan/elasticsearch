package com.elasticsearch.cn.elasticsearch.service;

import com.elasticsearch.cn.elasticsearch.bean.SupportAddress;
import com.elasticsearch.cn.elasticsearch.dao.SupportAddressMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 项目名称：elasticsearch
 * 类名称：com.elasticsearch.cn.elasticsearch.service
 * 类描述：
 * 创建人：simonsfan
 * 创建时间：2018/9/25 11:21
 */
@Service
public class SupportAddressServiceImpl implements SupportAddressService {

    @Autowired
    private SupportAddressMapper supportAddressMapper;

    /**
     * 根据英文城市名获取当前城市
     *
     * @param map
     * @return
     */
    @Override
    public List<SupportAddress> getSupportAddressByName(Map<String,Object> map) {
        return supportAddressMapper.findByEnName(map);
    }

    /**
     * 根据英文城市名获取下面所有区域地
     *
     * @param map
     * @return
     */
    @Override
    public List<SupportAddress> findAllByLevelAndBelongTo(Map<String,Object> map) {
        return supportAddressMapper.findAllByLevelAndBelongTo(map);
    }
}
