package com.pinyougou.seckill.service.impl;

import com.pinyougou.common.utils.IdWorker;
import com.pinyougou.entity.QueueTag;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 多线程类
 * @author xiaobo
 * @package com.pinyougou.seckill.service.impl
 * @time 2019/6/18 0018 17:00
 * @Version: 1.0
 */
@Component
@Transactional
public class MultiThreadWork {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private RedisTemplate transRedisTemplate;

    /**
     * 多线程下单方法
     * @param seckillId  抢购商品id
     */
    @Async
    public void createOrder(Long seckillId){

        try {
            System.out.println("模拟当前业务处理时间较长，这里用时6秒...");
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //多线程采用右取拿出队列中排队的用户
        String userId = (String) redisTemplate.boundListOps("seckill_goods_order_queue_" + seckillId).rightPop();

        //调用方法先从redis中减少库存
        Long count = redisTemplate.boundHashOps("seckillStockCount").increment(seckillId, -1);
        if (count<0){
            //库存不足更改队列用户标识信息为NO_STOCK(库存不足)
            redisTemplate.boundHashOps("user_order_info_"+userId).put(seckillId, QueueTag.NO_STOCK);
            throw new RuntimeException("你来晚了一步，商品已抢购一空!");
        }

        //从redis中查询商品
        TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);

		/*if (seckillGoods == null || seckillGoods.getStockCount()<1){
			throw new RuntimeException("你来晚了一步，商品已抢购一空!");
		}*/
        try {
            //开启redis事务
            transRedisTemplate.multi();
            //扣减库存--秒杀首页显示
            seckillGoods.setStockCount(seckillGoods.getStockCount()-1);
            //将修改过的库存重新存入redis
            transRedisTemplate.boundHashOps("seckillGoods").put(seckillId, seckillGoods);

            //如果库存不足
		/*if (seckillGoods.getStockCount() == 0){
		    //更新到数据库
			seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
			//从redis中删除当前秒杀商品
			redisTemplate.boundHashOps("seckillGoods").delete(seckillId);
		}*/

		/*如果库存不足(在高并发的应用场景)
		高并发场景中，good.StockCount可能不准，此时seckillGoodsStockCount一定是准确的,
		所以以seckillGoodsCount为标准*/
            if (count == 0 ){
                seckillGoods.setStockCount(0);
                //更新到数据库
                seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
                //从redis中删除相关商品
                transRedisTemplate.boundHashOps("seckillGoods").delete(seckillId);
            }

            //在支付前先将订单保存到redis
            TbSeckillOrder seckillOrder = new TbSeckillOrder();
            seckillOrder.setId(idWorker.nextId());//秒杀订单ID
            seckillOrder.setCreateTime(new Date());
            seckillOrder.setMoney(seckillGoods.getCostPrice());//秒杀价格
            seckillOrder.setSeckillId(seckillId);//秒杀商品ID
            seckillOrder.setSellerId(seckillGoods.getSellerId()); //商家
            seckillOrder.setUserId(userId);//设置用户ID
            seckillOrder.setStatus("0");//状态
            //保存订单到redis
            transRedisTemplate.boundHashOps("seckillOrder").put(userId, seckillOrder);

            //保存订单后修改用户标识为以下单
            transRedisTemplate.boundHashOps("user_order_info_"+userId).put(seckillId, QueueTag.CREATE_ORDER);

            //提交事务
            transRedisTemplate.exec();
        } catch (Exception e) {
            //取消事务，放弃执行事务块内的所有命令。
            transRedisTemplate.discard();
            //抢购失败,回顾库存
            redisTemplate.boundHashOps("seckillStockCount").increment(seckillId, 1);
            //标识抢购,秒杀失败
            transRedisTemplate.boundHashOps("user_order_info_"+userId).put(seckillId, QueueTag.SECKILL_FAIL);
            //如果刚好在最后一个报异常，要把mysql数据库的数据也还原
            if (count == 0){
                //这里一定要把异常抛出去，让spring捕获到异常把mysql回滚
                throw new RuntimeException(e);
            }
            e.printStackTrace();
        }
        System.out.println("用户:" + userId + ",抢购商品id:" + seckillId + "，下单成功,等待用户支付！");
    }
}
