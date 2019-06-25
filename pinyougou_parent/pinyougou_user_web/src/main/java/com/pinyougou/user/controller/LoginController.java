package com.pinyougou.user.controller;

import org.apache.commons.collections.map.HashedMap;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {

    @RequestMapping("/name")
    public Map showName(){
        //获取登入名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Map map = new HashedMap();
        map.put("username", username);
        return map;
    }
}
