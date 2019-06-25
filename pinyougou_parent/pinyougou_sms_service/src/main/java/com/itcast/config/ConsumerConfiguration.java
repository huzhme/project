package com.itcast.config;

import com.itcast.listener.MessageListener;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 消费者配置信息
 */
@Configuration
public class ConsumerConfiguration {

    @Autowired
    private MessageListener messageListener;

    @Bean
    public DefaultMQPushConsumer getPushConsumer(){
        DefaultMQPushConsumer consumer = null;

        try {
            //创建消费者
            consumer = new DefaultMQPushConsumer("sms-consumer-group");
            //设置NameServer地址
            consumer.setNamesrvAddr("127.0.0.1:9876");
            //接收主题,标签
            consumer.subscribe("topic-sms", "*");
            //设置监听器
            consumer.setMessageListener(messageListener);
            //启动消费者
            consumer.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return consumer;
    }
}
