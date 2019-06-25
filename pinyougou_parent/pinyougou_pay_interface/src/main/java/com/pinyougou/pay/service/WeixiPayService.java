package com.pinyougou.pay.service;

import java.util.Map;

/**
 * 微信支付接口
 *
 * @author xiaobo
 * @package com.pinyougou.pay.service
 * @time 2019/6/14 0014 22:14
 * @Version: 1.0
 */
public interface WeixiPayService {
    /**
     * 生成微信支付二维码--调用微信支付统一下单API
     * @param out_trade_no 订单号
     * @param total_fee    金额(分)
     * @return
     */
    public Map createNavite(String out_trade_no, String total_fee);

    /**
     * 查询微信支付状态
     * @param out_trade_no
     * @return
     */
    public Map queryPayStatus(String out_trade_no);

    /**
     * 根据订单号关闭微信订单
     * @param out_trade_no
     */
    public Map closePay(String out_trade_no);
}
