package com.elasticsearch.cn.elasticsearch.service;

import com.elasticsearch.cn.elasticsearch.bean.User;

import java.util.List;
import java.util.Map;

/**
 * 项目名称：elasticsearch
 * 类名称：com.elasticsearch.cn.elasticsearch.service
 * 类描述：
 * 创建人：simonsfan
 * 创建时间：2018/9/20 15:23
 */
public interface UserService {

    List<User> getUsers(Map<String,Object> map);

}
