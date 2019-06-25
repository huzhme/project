package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.entity.Cart;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车服务实现类
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 合并购物车
     * @param cartList1 cookie中的数据
     * @param cartList2 redis中的数据
     * @return 返回cartList1 合并后的数据
     */
    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        for (Cart cart : cartList2) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                //调用添加购物车方法
                this.addGoodsToCartList(cartList1, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return cartList1;
    }

    /**
     * 从redis中获取购物车列表
     * @param username
     * @return
     */
    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("从redis中获取购物车列表数据"+username);
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if (cartList == null){
            return new ArrayList<>();
        }
        return cartList;
    }

    /**
     * 将购物车列表存入redis
     * @param username
     * @param cartList
     */
    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("向redis中存放购物车列表数据"+username);
        redisTemplate.boundHashOps("cartList").put(username, cartList);
    }

    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1.根据商品SKU ID查询SKU商品信息
        TbItem tbItem = itemMapper.selectByPrimaryKey(itemId);
        if (tbItem == null) {
            throw new RuntimeException("商品信息不存在");
        }
        if (!"1".equals(tbItem.getStatus())) {
            throw new RuntimeException("商品状态无效");
        }

        //2.获取商家ID
        String sellerId = tbItem.getSellerId();

        //3.根据商家ID判断购物车列表中是否存在该商家的购物车
        Cart cart = this.searchCarBySellerId(cartList, sellerId);//抽取出一个方法用于判断商家ID是否存在于购物车

        //4.如果购物车列表中不存在该商家的购物车
        if (cart == null) {
            //4.1 新建购物车对象
            cart = new Cart();
            //4.2 将新建的购物车对象添加到购物车列表
            cart.setSellerId(sellerId);
            cart.setSellerName(tbItem.getSeller());
            //新建此商家购物车列表对象
            TbOrderItem orderItem = this.createOrderItem(tbItem, num);//抽取方法,用于构建商家购物车列表对象
            List<TbOrderItem> orderItemList = new ArrayList<>();
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            cartList.add(cart);
        } else {//5.如果购物车列表中存在该商家的购物车
            // 查询购物车明细列表中是否存在该商品
            TbOrderItem orderItem = this.searchOrderItemByItemId(cart.getOrderItemList(), itemId);//抽取方法,用于判断购物车明细列表是否存在该商品
            //5.1. 如果没有，新增购物车明细
            if (orderItem == null){
                orderItem = this.createOrderItem(tbItem, num);
                //将商品详情信息添加到cart购物车列表
                cart.getOrderItemList().add(orderItem);

            }else {//5.2. 如果有，在原购物车明细上添加数量，更改金额
                orderItem.setNum(orderItem.getNum()+num);
                //重新计算商品小计金额
                orderItem.setTotalFee(new BigDecimal(orderItem.getNum()*orderItem.getPrice().doubleValue()));
                //判断数量num计算结果是否<=0,成立则移除这条商品信息
                if (orderItem.getNum()<=0){
                    //移除这条商品信息
                    cart.getOrderItemList().remove(orderItem);
                }
                //判断移除商品后该商家下是有还有商品,如没有则移除这个商家下的购物信息cart
                if (cart.getOrderItemList().size() == 0){
                    //则移除这个商家下的购物信息cart
                    cartList.remove(cart);
                }
            }

        }

        return cartList;
    }

    /**
     * 跟据商品ID判断购物车明细列表是否存在该商品
     *
     * @param orderItemList 购物车明细列表
     * @param itemId        商品ID
     * @return
     */
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem orderItem : orderItemList) {
            if (itemId.longValue() == orderItem.getItemId().longValue()){
                return orderItem;
            }
        }
        return null;
    }

    /**
     * 创建订单明细
     *
     * @param tbItem 商品具体的信息
     * @param num    购买的数量
     * @return
     */
    private TbOrderItem createOrderItem(TbItem tbItem, Integer num) {
        if (num <= 0) {
            throw new RuntimeException("数量非法!,请勿操作!");
        }
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setItemId(tbItem.getId());
        orderItem.setGoodsId(tbItem.getGoodsId());
        orderItem.setTitle(tbItem.getTitle());
        orderItem.setPrice(tbItem.getPrice());
        orderItem.setNum(num);
        //小计,商品数量的乘机
        orderItem.setTotalFee(new BigDecimal(tbItem.getPrice().doubleValue() * num));
        orderItem.setPicPath(tbItem.getImage());
        orderItem.setSellerId(tbItem.getSellerId());
        return orderItem;
    }

    /**
     * 判断商家ID是否存在于购物车列表
     *
     * @param cartList 原来的购物车列表
     * @param sellerId 商家ID
     * @return 找到则返回这个cart对象
     */
    private Cart searchCarBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if (sellerId.equals(cart.getSellerId())) {
                return cart;
            }
        }
        return null;
    }
}
