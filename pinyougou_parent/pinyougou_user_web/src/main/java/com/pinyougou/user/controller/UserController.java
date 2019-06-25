package com.pinyougou.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.common.utils.PhoneFormatCheckUtils;
import com.pinyougou.entity.ResultInfo;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.user.service.UserService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Reference
    private UserService userService;

    @RequestMapping("/add")
    public ResultInfo add(@RequestBody TbUser user,String code){
        try {
            boolean checkSmsCode = userService.checkSmsCode(user.getPhone(),code);
            if (checkSmsCode == false){
                return new ResultInfo(false, "验证码输入错误!");
            }
            userService.add(user);
            return new ResultInfo(true, "注册成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultInfo(false, "注册失败");
        }
    }

    @RequestMapping("/sendCode")
    public ResultInfo sendCode(String phone){
        if (!PhoneFormatCheckUtils.isPhoneLegal(phone)){
            return new ResultInfo(false, "手机号不符合要求!");
        }
        try {
            userService.createSmsCode(phone);
            return new ResultInfo(true, "验证码发送成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultInfo(false, "验证码发送失败!");
        }
    }
}
