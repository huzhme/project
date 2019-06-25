package com.pinyougou.manager.mq;

import com.alibaba.fastjson.JSON;
import com.pinyougou.entity.MessageInfo;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageSender {

    @Autowired
    private DefaultMQProducer producer;

    public void sendMessage(MessageInfo info){
        try {
            //将消息对象转换为json字符串发送
            String content = JSON.toJSONString(info);

            //创建消息message = new Message
            Message message = new Message(
                    info.getTopic(),
                    info.getTags(),
                    info.getKeys(),
                    content.getBytes(RemotingHelper.DEFAULT_CHARSET)
            );
            //发送消息 producer.send(message)
            producer.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
