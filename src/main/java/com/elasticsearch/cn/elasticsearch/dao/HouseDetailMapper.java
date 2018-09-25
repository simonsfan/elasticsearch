package com.elasticsearch.cn.elasticsearch.dao;

import com.elasticsearch.cn.elasticsearch.bean.HouseDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface HouseDetailMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(HouseDetail record);

    int insertSelective(HouseDetail record);

    HouseDetail selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(HouseDetail record);

    int updateByPrimaryKey(HouseDetail record);

    List<HouseDetail> getHouseDetailByHouseId(@Param("houseId") String houseId);
}
