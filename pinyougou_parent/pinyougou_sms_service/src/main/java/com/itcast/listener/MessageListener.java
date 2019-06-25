package com.itcast.listener;

import com.alibaba.fastjson.JSON;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.itcast.utils.SmsUtils;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

/**
 * 消息监听器
 */
@Component
public class MessageListener implements MessageListenerConcurrently {

    @Autowired
    private SmsUtils smsUtils;

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        try {
            //循环读取消息-message
            //String mobile, String signName, String templateCode, String templateParam
            for (MessageExt msg : list) {
                //内容
                String json = new String(msg.getBody(), RemotingHelper.DEFAULT_CHARSET);
                //将json转换为Map对象
                Map<String,String> map = JSON.parseObject(json, Map.class);
                //调用阿里大于接口发短信
                SendSmsResponse response = smsUtils.sendSms(
                        map.get("mobile"),
                        map.get("signName"),
                        map.get("templateCode"),
                        map.get("templateParam")
                );
                //4、输出短信发送结果
                System.out.println("Code=" + response.getCode());
                System.out.println("Message=" + response.getMessage());
                System.out.println("RequestId=" + response.getRequestId());
                System.out.println("BizId=" + response.getBizId());

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //返回消息读取状态-CONSUME_SUCCESS
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}
