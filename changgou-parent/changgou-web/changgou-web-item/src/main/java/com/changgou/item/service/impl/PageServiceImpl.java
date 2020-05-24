package com.changgou.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.CategoryFeign;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.item.service.PageService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PageServiceImpl implements PageService {


    @Autowired
    private SpuFeign spuFeign;

    @Autowired
    private CategoryFeign categoryFeign;

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private TemplateEngine templateEngine;

    //生成静态文件路径
    @Value("${pagepath}")
    private String pagepath;




    /**
     * 构建数据模型
     * @param spuId
     * @return
     */
    private Map<String,Object>  buildDataModel(Long spuId) throws  Exception{
        //构建数据模型
        Map<String,Object> dateMap = new HashMap<>();
        //获取spu 和SKU列表
        Result<Spu> result = spuFeign.findById(spuId);
        Spu spu = result.getData();
        //获取分类信息
        dateMap.put("category1",categoryFeign.findById(spu.getCategory1Id()).getData());
        dateMap.put("category2",categoryFeign.findById(spu.getCategory2Id()).getData());
        dateMap.put("category3",categoryFeign.findById(spu.getCategory3Id()).getData());

        if (spu.getImages()!=null){
            dateMap.put("imageList",spu.getImages().split(","));
        }


         dateMap.put("specificationList", JSON.parseObject(spu.getSpecItems(),Map.class));
             dateMap.put("spu",spu);
        //根据spuId查询Sku集合
        Sku skuCondition = new Sku();
        skuCondition.setSpuId(spu.getId());

            Result<List<Sku>> resultSku = skuFeign.findList(skuCondition);
        dateMap.put("skuList",resultSku.getData());
        return dateMap;



    }


    /***
     * 生成静态页
     * @param spuId
     */

    @Override
    public void createPageHtml(Long spuId) {
        // 1.上下wen
        Context context = new Context();
        Map<String, Object> dataModel = null;
        try {
            dataModel = buildDataModel(spuId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        context.setVariables(dataModel);
        // 2.准备文件
        File dir = new File(pagepath);
        if (!dir.exists()){
            dir.mkdirs();
        }

        File dest = new File(dir,spuId+".html");
        // 3.生成页面
        try {
            PrintWriter writer = new PrintWriter(dest,"UTF-8");
            templateEngine.process("item",context,writer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }
}
