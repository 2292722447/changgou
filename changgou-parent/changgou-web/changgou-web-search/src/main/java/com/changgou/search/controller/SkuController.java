package com.changgou.search.controller;


import com.changgou.search.feign.SkuFeign;
import com.changgou.search.pojo.SkuInfo;
import entity.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping("/search")
public class SkuController {

    @Autowired
    private SkuFeign skuFeign;

    /**
     * 搜索
     * @param searchMap
     * @return
     */
    @GetMapping(value = "/list")
    public String search(@RequestParam(required = false) Map<String,String> searchMap, Model model) throws Exception {

        //调用changgou-service-search微服务
        Map<String,Object> resultMap = skuFeign.search(searchMap);
        model.addAttribute("result",resultMap);
        //计算分页
        Page<SkuInfo> pageInfo;
        pageInfo = new Page<SkuInfo>(
              Long.parseLong( resultMap.get("total").toString()),
                Integer.parseInt(resultMap.get("pageNumber").toString())+1,
                Integer.parseInt(resultMap.get("pageSize").toString())
        );

        model.addAttribute("pageInfo",pageInfo);
        //条件回显
        model.addAttribute("searchMap",searchMap);
        //获取上次请求的地址   //2个url 一个带排序 一个不带排序
        String[] urls = url(searchMap);
        model.addAttribute("url",urls[0]);
        model.addAttribute("sorturl",urls[1]);


        return "search";
    }


    /**
     * 拼接组装用户请求的URL地址
     * 获取用户每次请求的地址
     * 页面余姚在这次请求上添加额外的搜索地址
     * http://localhost:18086/search/lis
     * http://localhost:18086/search/list?category=华为
     * http://localhost:18086/search/list?category=华为&brand=小米
     */
    public String[] url(Map<String,String> searchMap){
         String url = "/search/list";
         String sorturl = "/search/list"; //排序地址


         if (searchMap!=null&&searchMap.size()>0){
             url+= "?";
             sorturl+= "?";
             for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                 //key是搜索的条件对象
                 String key = entry.getKey();

                 if (key.equalsIgnoreCase("pageNum")){
                     continue;
                 }
                 //value是搜索的值
                 String value = entry.getValue();
                 url+="="+value+"&";
                 //排序参数 跳过
                 if (key.equalsIgnoreCase("sortField")||key.equalsIgnoreCase("sortRule")){
                     continue;
                 }
                 sorturl+="="+value+"&";
             }
             //去掉最后一个&
             url=url.substring(0,url.length()-1);
             sorturl=sorturl.substring(0,url.length()-1);
         }
         return new String[]{url,sorturl};
    }
}
