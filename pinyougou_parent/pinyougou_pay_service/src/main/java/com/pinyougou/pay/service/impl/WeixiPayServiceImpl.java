package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.common.utils.HttpClient;
import com.pinyougou.pay.service.WeixiPayService;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xiaobo
 * @package com.pinyougou.pay.service.impl
 * @time 2019/6/14 0014 22:16
 * @Version: 1.0
 */
@Service
public class WeixiPayServiceImpl implements WeixiPayService{

    @Value("${appid}")
    private String appid;//微信公众账号或开放平台APP的唯一标识

    @Value("${partner}")
    private String partner; //财付通平台的商户账号

    @Value("${partnerkey}")
    private String partnerkey;//财付通平台的商户密钥

    @Value("${notifyurl}")
    private String notifyurl;//回调地址

    /**
     * 生成微信支付二维码--调用微信支付统一下单API
     * @param out_trade_no 订单号
     * @param total_fee    金额(分)
     * @return
     */
    @Override
    public Map createNavite(String out_trade_no, String total_fee) {
        Map map = new HashMap<>();

        try {
            //1.包装微信接口所需要的参数
            Map<String,String> params = new HashMap<>();
            params.put("appid", appid);//公众账号ID
            params.put("mch_id", partner);//商户号
            params.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
            //params.put("sign", );//签名
            params.put("body", "品优购");//商品描述,扫码后用户看到的商品信息
            params.put("out_trade_no", out_trade_no);//商户订单号
            params.put("total_fee", total_fee);//金额
            params.put("spbill_create_ip", "127.0.0.1");//终端IP
            params.put("notify_url", notifyurl);//回调地址
            params.put("trade_type", "NATIVE");//交易类型--NATIVE -Native支付

            //2.生成xml,通过工具类HttpClient发送请求得到数据
            String xmlParam = WXPayUtil.generateSignedXml(params, partnerkey);
            System.out.println("请求参数XML类型"+xmlParam);
            //后台发送http请求微信支付URL
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);
            httpClient.post();

            //3.解析结果
            String xmlResult = httpClient.getContent();
            System.out.println("调用微信URL返回结果:"+xmlResult);

            //调用微信SDK工具类解析xml转换为Map
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xmlResult);
            //返回结果
            map.put("code_url", resultMap.get("code_url"));//二维码链接
            map.put("out_trade_no", out_trade_no);//订单号
            map.put("total_fee", total_fee);//总金额

        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 查询微信支付状态
     * @param out_trade_no
     * @return
     */
    @Override
    public Map queryPayStatus(String out_trade_no) {
        try {
            //1.包装查询微信支付状态接口所需要的参数
            Map<String,String> params = new HashMap<>();
            params.put("appid", appid);//公众账号ID
            params.put("mch_id", partner);//商户号
            params.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
            //params.put("sign", );//签名
            params.put("out_trade_no", out_trade_no);//商户订单号

            //2.生成xml,通过工具类HttpClient发送请求得到数据
            String xmlParam = WXPayUtil.generateSignedXml(params, partnerkey);
            System.out.println("请求参数XML类型"+xmlParam);
            //后台发送http请求微信支付URL
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);
            httpClient.post();

            //3.解析结果
            String xmlResult = httpClient.getContent();
            System.out.println("调用微信URL返回结果:"+xmlResult);

            //调用微信SDK工具类解析xml转换为Map
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xmlResult);
            return resultMap;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据订单号关闭微信订单
     * @param out_trade_no
     */
    @Override
    public Map closePay(String out_trade_no) {
        try {
            //1.包装微信接口所需要的参数
            Map<String,String> params = new HashMap<>();
            params.put("appid", appid);//公众账号ID
            params.put("mch_id", partner);//商户号
            params.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
            params.put("out_trade_no", out_trade_no);//商户订单号

            //2.生成xml,通过工具类HttpClient发送请求得到数据
            String xmlParam = WXPayUtil.generateSignedXml(params, partnerkey);
            System.out.println("请求参数XML类型"+xmlParam);
            //后台发送http请求微信支付URL
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/closeorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);
            httpClient.post();

            //3.解析结果
            String xmlResult = httpClient.getContent();
            System.out.println("调用微信URL返回结果:"+xmlResult);

            //调用微信SDK工具类解析xml转换为Map
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xmlResult);
            //返回结果
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
