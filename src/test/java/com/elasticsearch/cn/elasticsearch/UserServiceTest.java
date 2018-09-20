package com.elasticsearch.cn.elasticsearch;

import com.elasticsearch.cn.elasticsearch.bean.User;
import com.elasticsearch.cn.elasticsearch.service.UserService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 项目名称：elasticsearch
 * 类名称：com.elasticsearch.cn.elasticsearch
 * 类描述：
 * 创建人：simonsfan
 * 创建时间：2018/9/20 17:32
 */
public class UserServiceTest extends ElasticsearchApplicationTests {

    @Autowired
    private UserService userService;

    @Test
    public void testGetUserList() {

        Map<String,Object> map = new HashMap<>();
        List<User> users = userService.getUsers(map);

        for (User user : users) {
            System.out.println(user.getName());
        }

    }

}
