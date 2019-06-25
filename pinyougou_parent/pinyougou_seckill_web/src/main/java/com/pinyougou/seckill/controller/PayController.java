package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.entity.ResultInfo;
import com.pinyougou.pay.service.WeixiPayService;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付控制层
 * @author xiaobo
 * @package com.pinyougou.cart.controller
 * @time 2019/6/15 0015 15:46
 * @Version: 1.0
 */
@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private WeixiPayService weixiPayService;

    @Reference
    private SeckillOrderService seckillOrderService;

    /**
     * 生成微信支付二维码--调用微信支付统一下单API
     * @return
     */
    @RequestMapping("/createNavite")
    public Map createNavite(){
        //获取当前登入的用户名用于查询redis中的数据
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        //到redis查询秒杀订单信息
        TbSeckillOrder seckillOrder = seckillOrderService.searchOrderFromRedisByUserId(userId);
        Map map = new HashMap();
        if (seckillOrder != null){
            //秒杀金额(分)
            String fen = (long)(seckillOrder.getMoney().doubleValue()*100)+"";
            //调用微信支付统一下单API
            map = weixiPayService.createNavite(seckillOrder.getId()+"", fen);
            return map;
        }else {
            return (Map) map.put("message", "订单错误!");
        }

    }

    /**
     * 查询微信支付状态
     * @param out_trade_no
     * @return
     */
    @RequestMapping("/queryPayStatus")
    public ResultInfo queryPayStatus(String out_trade_no){
        //获取登入的用户名
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        ResultInfo resultInfo = null;
        int i =1;
        //循环查询支付是否完成
        try {
            while (true){
                //调用查询接口
                Map<String,String> map = weixiPayService.queryPayStatus(out_trade_no);
                if (map == null){
                    resultInfo = new ResultInfo(false, "支付错误!");
                    break;
                }
                //如果支付状态为SUCCESS
                if (map.get("trade_state").equals("SUCCESS")){
                    //支付成功将秒杀订单保存到数据库
                    seckillOrderService.saveOrderFromRedisToDb(userId, map.get("transaction_id"));
                    resultInfo = new ResultInfo(true, "支付成功!");
                    break;
                }
                try {
                    //睡三秒再查询,避免被微信封ip
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //为了不让循环无休止地运行，我们定义一个循环变量，
                // 如果这个变量超过了这个值则退出循环，设置时间为5分钟
                i++;
                //300秒为5分钟，3秒执行一次，所以i >= 100约5分钟
                if (i>3){
                    //清除缓存订单与还原库存
                    //seckillOrderService.deleteOrderFromRedis(userId, new Long(out_trade_no));

                    //1.调用微信的关闭订单接口
                    Map<String,String> resultMap = weixiPayService.closePay(out_trade_no);
                    if ("FAIL".equals(resultMap.get("result_code"))){
                        //如果订单已被支付
                        if ("ORDERPAID".equals(resultMap.get("err_code"))){
                            resultInfo = new ResultInfo(true, "订单已被支付");
                            //正常发货,将秒杀订单保存到数据库
                            seckillOrderService.saveOrderFromRedisToDb(userId, map.get("transaction_id"));
                        }
                    }
                    if (resultInfo == null){
                        System.out.println("超时，取消订单");
                        //清除缓存订单与还原库存
                        seckillOrderService.deleteOrderFromRedis(userId, new Long(out_trade_no));
                    }
                    resultInfo = new ResultInfo(false, "支付已超时");

                    break;
                }
                System.out.println(i);
            }
        } catch (RuntimeException e) {
            resultInfo = new ResultInfo(false, e.getMessage());
        }
        return resultInfo;
    }
}
