package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.common.utils.CookieUtil;
import com.pinyougou.entity.Cart;
import com.pinyougou.entity.ResultInfo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;
    
    @Autowired
    private HttpServletResponse response;

    /**
     * 查询购物车列表
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {
        //获取登入的用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        //定义集合存cookie和redis的购物车集合
        List<Cart> carts = new ArrayList<>();

        //从cookie中取出购物车数据
        String cookie_cartListStr = CookieUtil.getCookieValue(request, "cartList", true);

        //如果cookie中购物车数据不为空
        if (StringUtils.isNotBlank(cookie_cartListStr)){//如果不为空
            //把json串转为Cart对象
            carts = JSON.parseArray(cookie_cartListStr, Cart.class);
        }

        //如果用户未登入
        if ("anonymousUser".equals(username)){
            System.out.println("从cookie中读取购物车...");
        }else {//以登入
            System.out.println("从redis中读取购物车");

            //从redis取出数据
            List<Cart> redis_cartList = cartService.findCartListFromRedis(username);
            //如果cookie中购物车数据为空
            if (carts.size() == 0){
                //将redis中的数据存入集合
                carts = redis_cartList;
            }else {
                System.out.println("合并购物车数据");
                //合并购物车,传入未登入时的cookie数据
                carts = cartService.mergeCartList(carts,redis_cartList);

                //更新redis中的数据
                cartService.saveCartListToRedis(username, carts);

                //删除cookie中的数据
                CookieUtil.deleteCookie(request, response, "cartList");

            }
        }
        return carts;
    }

    /**
     * 添加商品到购物车
     * @param itemId   商品ID
     * @param num      商品数量
     * @return 返回操作结果信息
     */
    @CrossOrigin(origins = "http://localhost:8084",allowCredentials = "true")
    @RequestMapping("/addGoodsToCartList")
    public ResultInfo addGoodsToCartList( Long itemId, Integer num) {
        try {
            //获取登入名
            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            //先查询原来商品的信息,从cookie中取出来
            List<Cart> cartList = this.findCartList();

            //重新调用addGoodsToCartList将原来的购物车数据传递下去
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);

            //未登入
            if ("anonymousUser".equals(username)){
                System.out.println("操作了cookie中的购物车");
                //将新的购物车列表存入cookie,存一天
                CookieUtil.setCookie(
                        request,
                        response,
                        "cartList",
                        JSON.toJSONString(cartList),
                        3600*24, //存入的时间
                        true);
            }else {
                //存入redis
                cartService.saveCartListToRedis(username, cartList);
                System.out.println("操作了redis中的购物车");
            }

            return new ResultInfo(true, "添加购物车成功!");
        } catch (RuntimeException e) {
            //提示系统service返回的消息
            return new ResultInfo(false, e.getMessage());
        }
    }

}
