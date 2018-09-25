package com.elasticsearch.cn.elasticsearch.service;

import com.elasticsearch.cn.elasticsearch.bean.HousePicture;
import com.elasticsearch.cn.elasticsearch.dao.HousePictureMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 项目名称：elasticsearch
 * 类名称：com.elasticsearch.cn.elasticsearch.service
 * 类描述：
 * 创建人：simonsfan
 * 创建时间：2018/9/25 18:42
 */
@Service
public class HousePictureServiceImpl implements HousePictureService {

    @Autowired
    private HousePictureMapper housePictureMapper;


    @Override
    public List<HousePicture> getHousePictureByHouseId(String houseId) {
        return housePictureMapper.getHousePictureByHouseId(houseId);
    }
}
