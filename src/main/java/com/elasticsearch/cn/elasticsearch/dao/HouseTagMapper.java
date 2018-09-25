package com.elasticsearch.cn.elasticsearch.dao;

import com.elasticsearch.cn.elasticsearch.bean.HouseTag;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface HouseTagMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(HouseTag record);

    int insertSelective(HouseTag record);

    HouseTag selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(HouseTag record);

    int updateByPrimaryKey(HouseTag record);

    List<HouseTag> getHouseTagByHouseId(@Param("houseId") String houseId);
}
