package com.pinyougou.page.listener;

import com.alibaba.fastjson.JSON;
import com.pinyougou.entity.EsItem;
import com.pinyougou.entity.MessageInfo;
import com.pinyougou.page.service.ItemPageService;
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
    private ItemPageService itemPageService;

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
                //如果是添加索引操作
                if (info.getMethod() == MessageInfo.METHOD_ADD){
                    List<EsItem> esItemList = JSON.parseArray(info.getContext().toString(), EsItem.class);
                    //批量生成静态页面
                    for (EsItem esItem : esItemList) {
                        itemPageService.getItemHtml(esItem.getGoodsId());
                    }
                }else if (info.getMethod() == MessageInfo.METHOD_DELETE){
                    //将接受的消息转换为集合数组
                    List<Long> idList = JSON.parseArray(info.getContext().toString(), Long.class);
                    //删除静态文件
                    for (Long goodsId : idList) {
                        //删除文件
                        new File(pagedir + goodsId +".html").delete();
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            //再读一次
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
        //返回消息读取状态-CONSUME_SUCCESS
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}
