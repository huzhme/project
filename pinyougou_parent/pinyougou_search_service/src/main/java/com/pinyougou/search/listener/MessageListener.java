package com.pinyougou.search.listener;

import com.alibaba.fastjson.JSON;
import com.pinyougou.entity.EsItem;
import com.pinyougou.entity.MessageInfo;
import com.pinyougou.search.service.EsItemService;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * 索引库更新-监听器
 */
public class MessageListener implements MessageListenerConcurrently {

    @Autowired
    private EsItemService esItemService;

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
                    //导入索引库
                    esItemService.importList(esItemList);
                }else if (info.getMethod() == MessageInfo.METHOD_DELETE){
                    //将接受的消息转换为集合数组
                    List<Long> idList = JSON.parseArray(info.getContext().toString(), Long.class);
                    //将集合转换为数组
                    Long[] ids = idList.toArray(new Long[idList.size()]);
                    //删除索引库
                    esItemService.deleteByGoodsId(ids);
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
