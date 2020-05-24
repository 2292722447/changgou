package com.changgou.order.service.impl;

import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl  implements CartService {


    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SpuFeign spuFeign;

    @Autowired  //数据存入那台redis
    private RedisTemplate redisTemplate;
    /**
     * 加入购物车
     * @param num
     * @param id
     */
    @Override
    public void add(Integer num, Long id,String username) throws Exception {

        //当购物车数量小于等于0时  需要移除该商品
         if (num<=0){
             //移除购物车该商品
             redisTemplate.boundHashOps("Cart_"+username).delete(id);

             //如果此时购物车数量为空 则连购物车 一起移除
             Long size = redisTemplate.boundHashOps("Cart_" + username).size();
             if (size==null && size<=0){
                 redisTemplate.boundHashOps("Cart_"+username).delete();
             }
             return;
         }
        //查询商品的详情
       //1 查询sku
        Result<Sku> skuResult = skuFeign.findById(id);
        Sku sku = skuResult.getData();
        //2 查询spu
        Result<Spu> spuResult = spuFeign.findById(sku.getSpuId());
        Spu spu = spuResult.getData();
        OrderItem orderItem = createOrderItem(num, id, sku, spu);
        //将购物车数据存入到reids
        redisTemplate.boundHashOps("Cart_"+username).put(id,orderItem);


    }


    /**
     * 购物车集合
     * @param username 用户登录名
     * @return
     */
    @Override
    public List<OrderItem> list(String username) {

        //获取指定命名空间下所有数据
        return redisTemplate.boundHashOps("Cart_"+username).values();
    }












    /**
     * 创建一个购物车对象
     * @param num
     * @param id
     * @param sku
     * @param spu
     * @return
     */
    private OrderItem createOrderItem(Integer num, Long id, Sku sku, Spu spu) {
        //将加入购物车的商品信息  封装成OrderItem
        OrderItem orderItem = new OrderItem();
        orderItem.setCategoryId1(spu.getCategory1Id());
        orderItem.setCategoryId2(spu.getCategory2Id());
        orderItem.setCategoryId3(spu.getCategory3Id());
        orderItem.setSpuId(spu.getId());
        orderItem.setSkuId(id);
        orderItem.setName(sku.getName());
        orderItem.setPrice(sku.getPrice());
        orderItem.setNum(num);
        orderItem.setMoney(num*orderItem.getPrice());
        orderItem.setImage(spu.getImage());
        return orderItem;
    }
}
