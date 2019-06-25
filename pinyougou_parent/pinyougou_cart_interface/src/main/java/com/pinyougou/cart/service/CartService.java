package com.pinyougou.cart.service;

import com.pinyougou.entity.Cart;

import java.util.List;

/**
 * 购物车服务接口
 */
public interface CartService {
    /**
     * 合并购物车
     * @param cartList1
     * @param cartList2
     * @return
     */
    public List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2);

    /**
     * 从redis中获取购物车列表
     * @param username
     * @return
     */
    public List<Cart> findCartListFromRedis(String username);

    /**
     * 将购物车列表存入redis
     * @param username
     * @param cartList
     */
    public void saveCartListToRedis(String username,List<Cart> cartList);

    /**
     * 添加商品到购物车
     * @param cartList 原来的购物车列表(用于做比对是否存在相同的商品,相同则数量+1)
     * @param itemId SKUID 商品id
     * @param num 购买数量
     * @return
     */
    public List<Cart> addGoodsToCartList(List<Cart> cartList,Long itemId,Integer num);
}
