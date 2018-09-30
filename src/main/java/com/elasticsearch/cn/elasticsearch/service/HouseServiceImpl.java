package com.elasticsearch.cn.elasticsearch.service;

import com.elasticsearch.cn.elasticsearch.bean.House;
import com.elasticsearch.cn.elasticsearch.bean.HouseDetail;
import com.elasticsearch.cn.elasticsearch.bean.HousePicture;
import com.elasticsearch.cn.elasticsearch.bean.HouseTag;
import com.elasticsearch.cn.elasticsearch.dao.HouseDetailMapper;
import com.elasticsearch.cn.elasticsearch.dao.HouseMapper;
import com.elasticsearch.cn.elasticsearch.dao.HousePictureMapper;
import com.elasticsearch.cn.elasticsearch.dao.HouseTagMapper;
import com.elasticsearch.cn.elasticsearch.dto.HouseDTO;
import com.elasticsearch.cn.elasticsearch.dto.HouseDetailDTO;
import com.elasticsearch.cn.elasticsearch.dto.HousePictureDTO;
import com.elasticsearch.cn.elasticsearch.form.RentSearch;
import com.elasticsearch.cn.elasticsearch.service.search.SearchService;
import org.apache.commons.collections.CollectionUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 项目名称：elasticsearch
 * 创建人：simonsfan
 * 创建时间：2018/9/25
 */
@Service
public class HouseServiceImpl implements HouseService {

    private static final Logger logger = LoggerFactory.getLogger(HouseServiceImpl.class);

    @Autowired
    private HouseMapper houseMapper;

    @Autowired
    private HouseTagMapper houseTagMapper;

    @Autowired
    private HousePictureMapper housePictureMapper;

    @Autowired
    private HouseDetailMapper houseDetailMapper;

    @Autowired
    private SearchService searchService;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<House> query(RentSearch rentSearch) {
        List<House> houseList = new ArrayList<>();
        try {
            if (rentSearch.getKeywords() != null && !rentSearch.getKeywords().isEmpty()) {
                List<Long> houseIdList = searchService.queryByEs(rentSearch);
                if (CollectionUtils.isEmpty(houseIdList)) {
                    return houseList;
                }
                for (Long houseId : houseIdList) {
                    House house = houseMapper.selectByPrimaryKey(houseId.intValue());
                    if (house != null) {
                        houseList.add(house);
                    }
                }
            } else {
                houseList = houseMapper.query(rentSearch);
            }
        } catch (Exception e) {
            logger.error("");
        }
        return houseList;
    }

    @Override
    public House getHouseByHouseId(Long houseId) {
        return houseMapper.selectByPrimaryKey(houseId.intValue());
    }

    @Override
    public HouseDTO findHouseOne(Long houseId) {
        HouseDTO houseDTO = new HouseDTO();
        //房源信息
        House house = houseMapper.selectByPrimaryKey(houseId.intValue());
        if(house == null){
            logger.error("");
            return houseDTO;
        }
        modelMapper.map(house, houseDTO);
        houseDTO.setId(Long.valueOf(house.getId()));

        //设置房源标签
        List<HouseTag> houseTags = houseTagMapper.getHouseTagByHouseId(String.valueOf(houseId));
        List<String> houseTagList = new ArrayList<>();
        for (HouseTag houseTag : houseTags) {
            houseTagList.add(houseTag.getName());
        }
        houseDTO.setTags(houseTagList);

        //设置房源照片
        List<HousePicture> housePictureList = housePictureMapper.getHousePictureByHouseId(String.valueOf(houseId));
        List<HousePictureDTO> pictures = new ArrayList<>();
        HousePictureDTO pictureDTO = new HousePictureDTO();
        for (HousePicture housePicture : housePictureList) {
            pictureDTO.setCdnPrefix(housePicture.getCdnPrefix());
            pictureDTO.setHeight(housePicture.getHeight());
            pictureDTO.setWidth(housePicture.getWidth());
            pictureDTO.setHouseId(Long.valueOf(housePicture.getHouseId()));
            pictureDTO.setPath(housePicture.getPath());
            pictureDTO.setId(Long.valueOf(housePicture.getId()));
            pictures.add(pictureDTO);
        }
        houseDTO.setPictures(pictures);

        //设置房源detail信息
        List<HouseDetail> houseDetailList = houseDetailMapper.getHouseDetailByHouseId(String.valueOf(houseId));
        HouseDetailDTO houseDetailDTO = new HouseDetailDTO();
        BeanUtils.copyProperties(houseDetailList.get(0), houseDetailDTO);
        houseDTO.setHouseDetail(houseDetailDTO);

        return houseDTO;
    }
}
