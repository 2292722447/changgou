package com.changgou.order.controller;


import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import entity.Result;
import entity.StatusCode;
import entity.TokenDecode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 购物车操作
 */

@RestController
@RequestMapping("/cart")
public class CartController {


    @Autowired
    private CartService cartService;
    /**
     * 加入页面
     * 1 加入购物车的数量
     * 2 商品id
     */
    @GetMapping("/add")
    public Result add(Integer num,Long id) throws Exception {
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        String username = userInfo.get("username");
         //加入购物车
        cartService.add(num,id,username);
        return new Result(true, StatusCode.OK,"加入购物车成功！");
    }

    /**
     * 购物车列表
     */
    @GetMapping("/list")
    public Result<List<OrderItem>> list(){

        //用户令牌信息  解析令牌信息 username
//        OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails();
//        String token = details.getTokenValue();

        Map<String, String> userInfo = TokenDecode.getUserInfo();
        String username = userInfo.get("username");
        //获取用户名
       // String username = "szitheima";
        //查询购物车列表
        List<OrderItem> orderItems = cartService.list(username);
        return new Result(true, StatusCode.OK,"购物车列表查询成功！",orderItems);
    }

}
