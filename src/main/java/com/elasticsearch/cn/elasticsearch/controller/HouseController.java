package com.elasticsearch.cn.elasticsearch.controller;

import com.elasticsearch.cn.elasticsearch.base.RentValueBlock;
import com.elasticsearch.cn.elasticsearch.bean.*;
import com.elasticsearch.cn.elasticsearch.dto.HouseDTO;
import com.elasticsearch.cn.elasticsearch.dto.HouseDetailDTO;
import com.elasticsearch.cn.elasticsearch.dto.HousePictureDTO;
import com.elasticsearch.cn.elasticsearch.form.RentSearch;
import com.elasticsearch.cn.elasticsearch.result.CommonResult;
import com.elasticsearch.cn.elasticsearch.service.*;
import com.elasticsearch.cn.elasticsearch.service.search.SearchService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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

    @Autowired
    private SearchService searchService;

    @GetMapping(value = {"/", "/index"})
    public String index(Model model) {
        return "index";
    }

    /**
     * 租房
     *
     * @param rentSearch
     * @param model
     * @param session
     * @param redirectAttributes
     * @return
     */
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
        Map<String, Object> map = new HashMap<>();
        map.put("enName", enName);
        map.put("level", SupportAddress.Level.CITY.getValue());
        List<SupportAddress> addresseList = supportAddressService.getSupportAddressByName(map);
        if (CollectionUtils.isEmpty(addresseList)) {
            redirectAttributes.addAttribute("msg", "must_chose_city");
            return "redirect:/index";
        }
        model.addAttribute("currentCity", addresseList.get(0));

        //根据城市名获取所有的区域信息
        Map<String, Object> map1 = new HashMap<>();
        map1.put("enName", enName);
        map1.put("level", SupportAddress.Level.REGION.getValue());
        List<SupportAddress> allRegionsList = supportAddressService.findAllByLevelAndBelongTo(map1);
        if (CollectionUtils.isEmpty(allRegionsList)) {
            redirectAttributes.addAttribute("msg", "must_chose_city");
            return "redirect:/index";
        }

        List<HouseDTO> houseDTOList = new ArrayList<>();

        // 关键的一步：
        //  1、如果用户有输入关键词，则用es快速搜索出关联的houseids，然后根据这些houseIds查询具体的信息  2、如果没有输入，则默认直接使用mysql查询
        // 注意: es中查询出来的顺序和经过mysql查询详细信息后的顺序要严格一致
        List<House> houseList = houseService.query(rentSearch);
        if (!CollectionUtils.isEmpty(houseList)) {
            for (House house : houseList) {
                HouseDTO houseDTO = new HouseDTO();
                BeanUtils.copyProperties(house, houseDTO);
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
                BeanUtils.copyProperties(houseDetailList.get(0), houseDetailDTO);
                houseDTO.setHouseDetail(houseDetailDTO);

                houseDTOList.add(houseDTO);
            }
        }
        model.addAttribute("total", houseDTOList.size());
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

    /**
     * 自动补全接口
     */
    @GetMapping("rent/house/autocomplete")
    @ResponseBody
    public CommonResult autocomplete(@RequestParam(value = "prefix") String prefix) {

        if (prefix.isEmpty()) {
            return CommonResult.success(-1,"bad request",null);
        }
        return this.searchService.suggest(prefix);
    }

    /**
     * 房详细页面
     *
     * @param houseId
     * @param model
     * @return
     */
    @GetMapping("rent/house/show/{id}")
    public String show(@PathVariable(value = "id") Long houseId,
                       Model model) {
        if (houseId <= 0) {
            return "404";
        }
        return "house-detail";
    }

    @ResponseBody
    @RequestMapping(value = "/test")
    public String test() {
        long houseId = 15l;
        searchService.remove(houseId);
        return "success";
    }


}
