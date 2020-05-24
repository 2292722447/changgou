package com.changgou.goods.feign;

import com.changgou.goods.pojo.Sku;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 描述
 *
 * @author www.itheima.com
 * @version 1.0
 * @package com.changgou.goods.feign *
 * @since 1.0
 */
@FeignClient(value="goods")
@RequestMapping("/sku")
public interface SkuFeign {
    /**
     * 查询符合条件的状态的SKU的列表
     * @return
     */
    @GetMapping
    public Result<List<Sku>> findAll() throws Exception;

    /**
     * 根据条件搜索的SKU的列表
     * @param sku
     * @return
     */
    @PostMapping(value = "/search" )
    public Result<List<Sku>> findList(@RequestBody(required = false) Sku sku) throws Exception;

    /**
     * 根据id查询商品
     * @param id
     * @return
     */

    @GetMapping("/{id}")
    Result<Sku> findById(@PathVariable Long id) throws Exception;


    /**
     * 商品信息递减
     * Map<key,value>  key：要递减的商品id
     *                    value:要递减的数量
     * @return
     */
    @GetMapping(value = "/decr/count")
    Result decrCount(@RequestParam Map<String,Integer> decrmap)throws Exception;

}
