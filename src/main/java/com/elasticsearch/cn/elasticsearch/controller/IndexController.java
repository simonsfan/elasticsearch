package com.elasticsearch.cn.elasticsearch.controller;

import com.elasticsearch.cn.elasticsearch.bean.User;
import com.elasticsearch.cn.elasticsearch.result.CommonResult;
import com.elasticsearch.cn.elasticsearch.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    private UserService userService;

    /**
     * 测试mybatis
     *
     * @param name
     * @return
     */
    @ResponseBody
    @RequestMapping("/hello")
    public CommonResult test(String name){
        Map<String,Object> map = new HashMap<>();
        map.put("name",name);
        List<User> users = userService.getUsers(map);
        return ResultUtil.success("0","success",users);
    }

    @RequestMapping("/index")
    public String index(){
        return "index";
    }

}
