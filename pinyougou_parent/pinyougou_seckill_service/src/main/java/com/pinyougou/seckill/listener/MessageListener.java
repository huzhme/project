package com.pinyougou.seckill.listener;

import com.alibaba.fastjson.JSON;
import com.pinyougou.entity.EsItem;
import com.pinyougou.entity.MessageInfo;
import com.pinyougou.seckill.service.SeckillGoodsService;
import com.pinyougou.seckill.service.impl.CreatePageService;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class MessageListener implements MessageListenerConcurrently {
    @Autowired
    private CreatePageService createPageService;

    @Value("${pagedir}")
    private String pagedir;

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        try {
            //循环读取消息-message
            for (MessageExt msg : list) {
                //内容
                String json = new String(msg.getBody(), RemotingHelper.DEFAULT_CHARSET);
                //将内容转换为MessageInfo对象
                MessageInfo info = JSON.parseObject(json, MessageInfo.class);
                //如果是添加静态文件操作
                if (info.getMethod() == MessageInfo.METHOD_ADD) {
                    //将接受的消息转换为集合数组
                    List<Long> idList = JSON.parseArray(info.getContext().toString(), Long.class);

                    //批量生成静态页面
                    for (Long id : idList) {
                        createPageService.buildHtml(id);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            //再读一次
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
        //返回消息读取状态-CONSUME_SUCCESS
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}
