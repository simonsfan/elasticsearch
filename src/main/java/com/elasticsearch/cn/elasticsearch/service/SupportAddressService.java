package com.elasticsearch.cn.elasticsearch.service;

import com.elasticsearch.cn.elasticsearch.bean.SupportAddress;

import java.util.List;
import java.util.Map;

/**
 * 项目名称：elasticsearch
 * 类名称：com.elasticsearch.cn.elasticsearch.service
 * 类描述：
 * 创建人：simonsfan
 * 创建时间：2018/9/25 11:20
 */
public interface SupportAddressService {

    public List<SupportAddress> getSupportAddressByName(Map<String,Object> map);

    public List<SupportAddress> findAllByLevelAndBelongTo(Map<String,Object> map);
}
