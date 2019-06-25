package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.common.utils.IdWorker;
import com.pinyougou.entity.ResultInfo;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixiPayService;
import com.pinyougou.pojo.TbPayLog;
import org.springframework.beans.factory.annotation.Autowired;
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
    private OrderService orderService;

    @Autowired
    private IdWorker idWorker;

    /**
     * 生成微信支付二维码--调用微信支付统一下单API
     * @return
     */
    @RequestMapping("/createNavite")
    public Map createNavite(){
        //获取当前登入的用户名用于查询redis中的数据
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        //到redis查询支付日志
        TbPayLog payLog = orderService.searchPayLogFromRedis(userId);
        Map map = new HashMap();
        if (payLog != null){
            map = weixiPayService.createNavite(payLog.getOutTradeNo(), payLog.getTotalFee()+"");
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
        ResultInfo resultInfo = null;
        int i =1;
        //循环查询支付是否完成
        while (true){
            //调用查询接口
            Map<String,String> map = weixiPayService.queryPayStatus(out_trade_no);
            if (map == null){
                resultInfo = new ResultInfo(false, "支付错误!");
                break;
            }
            //如果支付状态为SUCCESS
            if (map.get("trade_state").equals("SUCCESS")){
                //修改日志信息和关联的订单状态
                orderService.updateOrderStatus(out_trade_no, map.get("transaction_id"));
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
            if (i>100){
                resultInfo = new ResultInfo(false, "支付已超时");
                break;
            }
        }
        return resultInfo;
    }
}
