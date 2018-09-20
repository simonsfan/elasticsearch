package com.elasticsearch.cn.elasticsearch.dao;

import com.elasticsearch.cn.elasticsearch.bean.HousePicture;

public interface HousePictureMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(HousePicture record);

    int insertSelective(HousePicture record);

    HousePicture selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(HousePicture record);

    int updateByPrimaryKey(HousePicture record);
}
