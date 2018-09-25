package com.elasticsearch.cn.elasticsearch.controller;

import com.elasticsearch.cn.elasticsearch.base.RentValueBlock;
import com.elasticsearch.cn.elasticsearch.bean.*;
import com.elasticsearch.cn.elasticsearch.dto.HouseDTO;
import com.elasticsearch.cn.elasticsearch.dto.HouseDetailDTO;
import com.elasticsearch.cn.elasticsearch.dto.HousePictureDTO;
import com.elasticsearch.cn.elasticsearch.form.RentSearch;
import com.elasticsearch.cn.elasticsearch.service.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HouseController {

    @Autowired
    private SupportAddressService supportAddressService;

    @Autowired
    private HouseService houseService;

    @Autowired
    private HouseTagService houseTagService;

    @Autowired
    private HousePictureService housePictureService;

    @Autowired
    private HouseDetailService houseDetailService;

    @GetMapping(value = {"/", "/index"})
    public String index(Model model) {
        return "index";
    }

    @GetMapping("rent/house")
    public String rentHousePage(@ModelAttribute RentSearch rentSearch,
                                Model model, HttpSession session,
                                RedirectAttributes redirectAttributes) {
        if (rentSearch.getCityEnName() == null) {
            String cityEnNameInSession = (String) session.getAttribute("cityEnName");
            if (cityEnNameInSession == null) {
                redirectAttributes.addAttribute("msg", "must_chose_city");
                return "redirect:/index";
            } else {
                rentSearch.setCityEnName(cityEnNameInSession);
            }
        } else {
            session.setAttribute("cityEnName", rentSearch.getCityEnName());
        }

        //根据城市名获取当前城市信息
        String enName = rentSearch.getCityEnName();
        Map<String,Object> map = new HashMap<>();
        map.put("enName",enName);
        map.put("level", SupportAddress.Level.CITY.getValue());
        List<SupportAddress> addresseList = supportAddressService.getSupportAddressByName(map);
        if (CollectionUtils.isEmpty(addresseList)) {
            redirectAttributes.addAttribute("msg", "must_chose_city");
            return "redirect:/index";
        }
        model.addAttribute("currentCity", addresseList.get(0));

        //根据城市名获取所有的区域信息
        Map<String,Object> map1 = new HashMap<>();
        map1.put("enName",enName);
        map1.put("level", SupportAddress.Level.REGION.getValue());
        List<SupportAddress> allRegionsList = supportAddressService.findAllByLevelAndBelongTo(map1);
        if (CollectionUtils.isEmpty(allRegionsList)) {
            redirectAttributes.addAttribute("msg", "must_chose_city");
            return "redirect:/index";
        }

        List<HouseDTO> houseDTOList = new ArrayList<>();

        List<House> houseList = houseService.query(rentSearch);
        for (House house : houseList) {
            HouseDTO houseDTO = new HouseDTO();
            BeanUtils.copyProperties(house,houseDTO);
            houseDTO.setId(Long.valueOf(house.getId()));
            //设置房源标签
            List<HouseTag> houseTags = houseTagService.getHouseTagByHouseId(String.valueOf(house.getId()));
            List<String> houseTagList = new ArrayList<>();
            for (HouseTag houseTag : houseTags) {
                houseTagList.add(houseTag.getName());
            }
            houseDTO.setTags(houseTagList);

            //设置房源照片
            List<HousePicture> housePictureList = housePictureService.getHousePictureByHouseId(String.valueOf(house.getId()));
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
            List<HouseDetail> houseDetailList = houseDetailService.getHouseDetailByHouseId(String.valueOf(house.getId()));
            HouseDetailDTO houseDetailDTO = new HouseDetailDTO();
            BeanUtils.copyProperties(houseDetailList.get(0),houseDetailDTO);
            houseDTO.setHouseDetail(houseDetailDTO);

            houseDTOList.add(houseDTO);
        }

        model.addAttribute("total", houseList.size());
        model.addAttribute("houses", houseDTOList);

        if (rentSearch.getRegionEnName() == null) {
            rentSearch.setRegionEnName("*");
        }

        model.addAttribute("searchBody", rentSearch);
        model.addAttribute("regions", allRegionsList);

        model.addAttribute("priceBlocks", RentValueBlock.PRICE_BLOCK);
        model.addAttribute("areaBlocks", RentValueBlock.AREA_BLOCK);

        model.addAttribute("currentPriceBlock", RentValueBlock.matchPrice(rentSearch.getPriceBlock()));
        model.addAttribute("currentAreaBlock", RentValueBlock.matchArea(rentSearch.getAreaBlock()));

        return "rent-list";
    }


}
