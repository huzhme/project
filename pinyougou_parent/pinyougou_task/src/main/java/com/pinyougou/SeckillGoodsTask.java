package com.pinyougou;

import com.github.abel533.entity.Example;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * SpringTask
 * @author xiaobo
 * @package com.pinyougou
 * @time 2019/6/16 0016 11:48
 * @Version: 1.0
 */
@Component
public class SeckillGoodsTask {

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    //30秒执行一次
    @Scheduled(cron = "0/30 * * * * ?")
    public void refreshSeckillGoodsTask(){
        //构建条件
        Example example = new Example(TbSeckillGoods.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("status", "1");//审核状态
        criteria.andGreaterThan("stockCount", 0);//剩余库存数大于0
        //系统当前的时间
        Date nowTime = new Date();
        //开始时间小于等于当前时间
        criteria.andLessThanOrEqualTo("startTime", nowTime);
        //结束时间大于等于当前时间
        criteria.andGreaterThanOrEqualTo("endTime", nowTime);

        //处理BUG解决重复添加符合的商品到缓存redis
        Set ids = redisTemplate.boundHashOps("seckillGoods").keys();
        if (ids != null && ids.size()>0){
            //将set集合转换为List集合
            List idList = new ArrayList(ids);
            //将以添加过的id过滤
            criteria.andNotIn("id", idList);
        }

        //查询符合条件的商品添加到缓存redis
        List<TbSeckillGoods> seckillGoods = seckillGoodsMapper.selectByExample(example);
        if (seckillGoods != null && seckillGoods.size()>0){
            //把数据放入缓存
            for (TbSeckillGoods seckillGood : seckillGoods) {
                System.out.println("秒杀商品加入了缓存，id为：" + seckillGood.getId());
                redisTemplate.boundHashOps("seckillGoods").put(seckillGood.getId(), seckillGood);

                //把符合条件的商品所有的库存存入redis
                //increment(操作的key,操作的值(可以是负数))
                redisTemplate.boundHashOps("seckillStockCount").increment(seckillGood.getId(),seckillGood.getStockCount());
            }
        }else {
            System.out.println("本次定时任务，没有新的商品加入缓存...");
        }
    }

    //每秒执行
    /*@Scheduled(cron = "* * * * * ?")
    public void startTask(){
        System.out.println("执行了定时器"+new Date());
    }*/
}
