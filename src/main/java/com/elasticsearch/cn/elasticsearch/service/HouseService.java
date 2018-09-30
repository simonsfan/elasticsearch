package com.elasticsearch.cn.elasticsearch.service;

import com.elasticsearch.cn.elasticsearch.bean.House;
import com.elasticsearch.cn.elasticsearch.dto.HouseDTO;
import com.elasticsearch.cn.elasticsearch.form.RentSearch;

import java.util.List;

/**
 * 项目名称：elasticsearch
 * 类名称：com.elasticsearch.cn.elasticsearch.service
 * 类描述：
 * 创建人：simonsfan
 * 创建时间：2018/9/25 17:07
 */
public interface HouseService {

    List<House> query(RentSearch rentSearch);

    House getHouseByHouseId(Long houseId);

    HouseDTO findHouseOne(Long houseId);
}
