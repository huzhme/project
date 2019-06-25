package com.pinyougou.user.service;

import com.pinyougou.pojo.TbUser;

public interface UserService {
    /**
     * 注册方法
     * @param user
     */
    public void add(TbUser user);

    /**
     * 生成短信验证码
     * @param phone
     */
    public void createSmsCode(String phone);

    boolean checkSmsCode(String phone, String code);
}
