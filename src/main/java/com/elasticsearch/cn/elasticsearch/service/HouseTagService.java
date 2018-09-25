package com.elasticsearch.cn.elasticsearch.service;

import com.elasticsearch.cn.elasticsearch.bean.HouseTag;

import java.util.List;

/**
 * 项目名称：elasticsearch
 * 类名称：com.elasticsearch.cn.elasticsearch.service
 * 类描述：
 * 创建人：simonsfan
 * 创建时间：2018/9/25 18:29
 */
public interface HouseTagService {

    public List<HouseTag> getHouseTagByHouseId(String houseId);

}
