package com.pinyougou.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbUserMapper;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.user.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private TbUserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DefaultMQProducer producer;

    @Override
    public void add(TbUser user) {
        user.setCreated(new Date());//创建日期
        user.setUpdated(user.getCreated());//修改日期
        String password = DigestUtils.md5Hex(user.getPassword());//对密码加密
        user.setPassword(password);
        userMapper.insertSelective(user);
    }

    @Override
    public void createSmsCode(String phone) {
        //生成六位随机数
        String code = (long)(Math.random() * 1000000)+"";
        //存储以user_mobile_+手机号为key
        BoundValueOperations valueOperations = redisTemplate.boundValueOps("user_mobile" + phone);
        //保存验证码
        valueOperations.set(code);
        //设置有效时间为  1分钟
        valueOperations.expire(1, TimeUnit.MINUTES);

        //发送rockerMq消息
        try {
            //创建消息对象 String mobile, String signName, String templateCode, String templateParam
            Map<String,String> map = new HashMap<>();
            map.put("mobile", phone);
            map.put("signName", "AnXiaoBo");
            map.put("templateCode", "SMS_167401894");
            map.put("templateParam", "{\"code\":"+code+"}");
            //将Map转换为String
            String json = JSON.toJSONString(map);

            Message message = new Message(
                    "topic-sms",
                    "tags-sms-test",
                    "keys-sms-test",
                    json.getBytes(RemotingHelper.DEFAULT_CHARSET)
            );
            //发送消息
            producer.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean checkSmsCode(String phone, String code) {
        //读取验证码
        String smsCode = (String) redisTemplate.boundValueOps("user_mobile" + phone).get();
        //对比用户输入的验证码是否一样
        return code.equals(smsCode);
    }
}
