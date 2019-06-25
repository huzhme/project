package com.pinyougou.entity;

import java.io.Serializable;

/**
 * 排队标识对象
 * @author xiaobo
 * @package com.pinyougou.entity
 * @time 2019/6/18 0018 15:46
 * @Version: 1.0
 */
public enum  QueueTag implements Serializable {
    /**
     * 排队标识：正在排队
     */
    IN_LINE,
    /**
     * 排队标识：已下单
     */
    CREATE_ORDER,
    /**
     * 排队标识：支付成功
     */
    PAY_SUCCESS,
    /**
     * 排队标识：支付失败
     */
    PAY_FAIL,
    /**
     * 排队标识：秒杀失败
     */
    SECKILL_FAIL,
    /**
     * 排队标识：没有库存
     */
    NO_STOCK;

}
