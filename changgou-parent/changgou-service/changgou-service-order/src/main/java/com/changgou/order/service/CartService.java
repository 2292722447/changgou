package com.changgou.order.service;

import com.changgou.order.pojo.OrderItem;

import java.util.List;

public interface CartService {


    /**
     * 加入购物车
     * @param num
     * @param id
     * @param username
     * @throws Exception
     */
    void add(Integer num,Long id,String username) throws Exception;


    /**
     * 购物车集合查询
     * @param username 用户登录名
     * @return
     */
    List<OrderItem> list(String username);
}

