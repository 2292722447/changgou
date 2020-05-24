package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.dao.SkuEsMapper;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SkuService;
import entity.Result;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class SkuServiceImpl implements SkuService {


    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SkuEsMapper skuEsMapper;


    /**
     * ElasticsearchTempalte :可以实现索引库的CRUD
     */
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;


    /**
     * 多条件搜索
     * @param searchMap
     * @return
     */
    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {

        /**
         * 搜索条件封装
         */
        NativeSearchQueryBuilder nativeSearchQueryBuilder = buildBasicQuery(searchMap);

        //集合搜索
        Map<String, Object> resultMap = searchList(nativeSearchQueryBuilder);

        //当用户选择了分类 作为搜索条件，则不需要对分类进行分组搜索 因为分组搜索的数据是用于先生分类搜索条件的
        //分类分组查询
//        if (searchMap==null||StringUtils.isEmpty(searchMap.get("category"))){
//            List<String> categoryList = searchCategoryList(nativeSearchQueryBuilder);
//            resultMap.put("categoryList",categoryList);
//        }
//
//         //当用户选择了分类  将作为品牌条件，则不需要对分类进行分组搜索 因为分组搜索的数据是用于先生分类搜索条件的
//        //查询品牌集合
//        if (searchMap==null||StringUtils.isEmpty(searchMap.get("brand"))) {
//            List<String> brandList = searchBrandList(nativeSearchQueryBuilder);
//            resultMap.put("brandList", brandList);
//        }
//
//        //规格查询
//        Map<String, Set<String>> specList = searchSpecList(nativeSearchQueryBuilder);
//        resultMap.put("specList",specList);


        //分组搜索实现
        Map<String, Object> groupMap = searchGroupList(nativeSearchQueryBuilder, searchMap);
        resultMap.putAll(groupMap);
        return resultMap;
    }
    /**
     * 分类分组查询  根据分类分组  品牌分组   规格分组
     * @param nativeSearchQueryBuilder
     * @return
     */
    private Map<String,Object> searchGroupList(NativeSearchQueryBuilder nativeSearchQueryBuilder,Map<String,String> searchMap) {

        //定义一个Map存储所有分组结果
        Map<String,Object>  groupMapResult=  new HashMap<String,Object>();
        //添加一个分组查询
        if (searchMap==null||StringUtils.isEmpty(searchMap.get("category"))){
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuCategory").field("categoryName"));
        }
        if (searchMap==null||StringUtils.isEmpty(searchMap.get("brand"))){
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuBrand").field("brandName"));
        }

        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuSpec").field("spec.keyword"));
        //分页参数-总记录数
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);


        //获取分组数据  .get("SkuCategory"); 获取指域的集合数
        if (searchMap==null||StringUtils.isEmpty(searchMap.get("category"))){
        StringTerms categoryTerms = aggregatedPage.getAggregations().get("skuCategory");
            //获取分类分组集合数据
            List<String> categoryList = getGroupList(categoryTerms);
            groupMapResult.put("categoryList",categoryList);
        }

        if (searchMap==null||StringUtils.isEmpty(searchMap.get("brand"))){
        StringTerms brandTerms = aggregatedPage.getAggregations().get("skuBrand");
            //获取品牌分组集合数据
            List<String> brandList = getGroupList(brandTerms);
            groupMapResult.put("brandList",brandList);}
        StringTerms specTerms = aggregatedPage.getAggregations().get("skuSpec");


        //获取规格分组集合数据  合并
        List<String> specList = getGroupList(specTerms);
        Map<String, Set<String>> setMap = putAllSpec(specList);
        groupMapResult.put("specList",setMap);
             return groupMapResult;
    }





    /**
     * 获取分组集合数据
     * @param stringTerms
     * @return
     */
    public List<String> getGroupList(StringTerms stringTerms){
        List<String> groupList = new ArrayList<String>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String feildName = bucket.getKeyAsString();  //其中一个分类名字
            groupList.add(feildName);
        }
        return groupList;
    }

    /**
     * 搜索
     * @param searchMap
     * @return
     */
    private NativeSearchQueryBuilder buildBasicQuery(Map<String, String> searchMap) {
        //NativeSearchQuery 搜索条件构建对象 主要用于封装各种搜索条件
        NativeSearchQueryBuilder  nativeSearchQueryBuilder = new NativeSearchQueryBuilder();

        //BoolQuery  must,must_not
        //构建一个boolquery
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (searchMap!=null&&searchMap.size()>0) {
            //根据关键词搜索
            String keywords = searchMap.get("keywords");
            //如果关键词不为空 则搜索关键词
            if (!StringUtils.isEmpty(keywords)) {
                //nativeSearchQueryBuilder.withQuery(QueryBuilders.queryStringQuery(keywords).field("name"));
                boolQueryBuilder.must(QueryBuilders.queryStringQuery(keywords).field("name"));
            }

            //分类过滤
            if (!StringUtils.isEmpty(searchMap.get("category"))) {
                boolQueryBuilder.must(QueryBuilders.termQuery("categoryName",searchMap.get("category")));
            }
            //品牌过滤
            if (!StringUtils.isEmpty(searchMap.get("brand"))) {
                boolQueryBuilder.must(QueryBuilders.termQuery("brandName",searchMap.get("brand")));
            }
            //规格过滤   spec_
            for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                String key = entry.getKey();
                //如果ke以spec_则表示规格筛选查询
                if (key.startsWith("spec_")){
                    //规格条件的值
                    String value = entry.getValue();
                    //spec_网络  spec_前五个要去掉
                    boolQueryBuilder.must(QueryBuilders.termQuery("specMap."+key.substring(5)+".keyword",value));
                }
            }
            //price  0-500  500-1000 1000-1500 1500-2000
            String price = searchMap.get("price");
            if (!StringUtils.isEmpty(price)){
                //去掉中文元和以上 0-500  500-1000 1000-1500 1500-2000
                price=price.replace("元","").replace("以上","");
                //prices[] 根据-分割   [0,500]  [500,1000]  [1000,1500]
                String[] prices = price.split("-");
                //x一定不为空  y有可能为空
                if (prices!=null&&prices.length>0){
                     //prices[1]!==null price<=prices[1]
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("price").gt(Integer.parseInt(prices[0])));
                    if (prices.length==2){
                        boolQueryBuilder.must(QueryBuilders.rangeQuery("price").lte(Integer.parseInt(prices[1])));
                    }
                }
            }

        }
        //排序实现
        String sortField = searchMap.get("sortField");//指定排序的域
        String sortRule = searchMap.get("sortRule"); //指定排序的规则
        if (!StringUtils.isEmpty(sortField)&&!StringUtils.isEmpty(sortRule)){
            nativeSearchQueryBuilder.withSort(new FieldSortBuilder(sortField)//指定排序域
                    .order(SortOrder.valueOf(sortRule)));//指定排序规则
        }



        //分页 用户如果不传分页参数  默认第一页
         Integer pageNum = coverterPage(searchMap);  //默认第一页
          Integer size = 30; //默认查询的数据条数
        nativeSearchQueryBuilder.withPageable(PageRequest.of(pageNum-1,size));

        //将BoolQueryBuilder对象填充给nativeSearchQueryBuilder
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);
        return nativeSearchQueryBuilder;
    }

    /**
     * 接受前端传入的分页参数
     * @param searchMap
     * @return
     */
    public Integer coverterPage(Map<String,String> searchMap){

        if (searchMap!=null){
            String pageNum = searchMap.get("pageNum");

            try {
                return Integer.parseInt(pageNum);
            }catch (NumberFormatException e){

            }
        }
        return 1;
    }


    /**
     * 结果集搜索
     * @param nativeSearchQueryBuilder
     * @return
     */
    private Map<String, Object> searchList(NativeSearchQueryBuilder nativeSearchQueryBuilder) {

        //高亮配置
//        HighlightBuilder.Field field = new HighlightBuilder.Field("name");//指定高亮域
//        //前缀  <em style="color:red">
//        field.preTags("<em style=\"color:red;\">");
//        //后缀 </em>
//        field.postTags("</em>");
//        //碎片长度  关键词数据的上路   如  今天" <em style=\"color:red\">"小红" </em style=\"color:red\">"穿了一件花衣服  好美丽啊 好美丽啊 好美丽啊 好美丽啊 好美丽啊
//        field.fragmentSize(100);
//
//        //添加高亮
//        nativeSearchQueryBuilder.withHighlightFields();

        //设置高亮的字段 针对 商品的名称进行高亮
        nativeSearchQueryBuilder.withHighlightFields(new HighlightBuilder.Field("name"));
        //设置前缀 和 后缀
        nativeSearchQueryBuilder.withHighlightBuilder(new HighlightBuilder().preTags("<em style=\"color:red\">").postTags("</em>"));

        /**
         *1搜索条件封装
         * 2搜索的结果集
         *  page 搜索结果集的封装
         */
        //执行搜索,响应结果
        // ElasticsearchTempalte :可以实现索引库的CRUD
        //AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);
        AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(
                  nativeSearchQueryBuilder.build(),  //搜索条件封装
                 SkuInfo.class,//数据集合要转换的字节码
                new SearchResultMapper() { //执行搜索后 将数据结果集封装到该对象中
                    @Override
                    public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> aClass, Pageable pageable) {

                        //存储所有转换后的高亮数据
                        List<T>  list = new ArrayList<T>();
                        //执行查询 获取所有数据 -结果集 包含高亮数据和非高亮数据
                        for (SearchHit hit : response.getHits()) {
                            //分析结果集 获取非高亮数据
                           SkuInfo skuInfo=JSON.parseObject(hit.getSourceAsString(),SkuInfo.class);
                            //分析结果集 获取高亮数据 只有某个域的高亮数据
                            HighlightField highlightField = hit.getHighlightFields().get("name");
                            if (highlightField!=null && highlightField.getFragments()!=null){

                                //高亮数据读取出来
                                Text[] fragments = highlightField.getFragments();
                                StringBuffer buffer = new StringBuffer();
                                for (Text fragment : fragments) {
                                    buffer.append(fragment.toString());
                                }
                                //非高亮数据中指定的域替换成高亮数据
                                skuInfo.setName(buffer.toString());

                            }
                            //将高亮数据添加到集合中
                            list.add((T)skuInfo);
                        }
                        /**
                         * 搜索的集合数据  携带高亮
                         * 分页对象信息
                         * 搜索记录的总条数
                         *
                         */
                        return new AggregatedPageImpl<T>(list,pageable,response.getHits().getTotalHits());
                    }
                });
        //获取搜索封装信息
        NativeSearchQuery query = nativeSearchQueryBuilder.build();
        Pageable pageable = query.getPageable();
        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPageNumber();


        long totalElements = page.getTotalElements();
        //总页数
        int totalPages = page.getTotalPages();
        //分析数据
        //获取数据结果集

        List<SkuInfo> contents = page.getContent();
        //封装一个Map存储数据  然后 返回
        Map<String,Object> resultMap = new HashMap<String,Object>();
        resultMap.put("rows",contents);
        resultMap.put("total",totalElements);
        resultMap.put("totalPages",totalPages);
        //分页数据
        resultMap.put("pageSize",pageSize);
        resultMap.put("pageNumber",pageNumber);
        return resultMap;
    }
    /**
     * 规格分组查询
     * @param nativeSearchQueryBuilder
     * @return
     */
    private Map<String, Set<String>> searchSpecList(NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        //添加一个分组查询
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuSpec").field("spec.keyword").size(10000));
        //分页参数-总记录数
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);
        //获取分组数据  .get("SkuCategory"); 获取指域的集合数
        StringTerms stringTerms = aggregatedPage.getAggregations().get("skuSpec");
        List<String> specList = new ArrayList<String>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String specName = bucket.getKeyAsString();  //其中一个规格名字
            specList.add(specName);
        }
        Map<String, Set<String>> allSpec = putAllSpec(specList);
        return allSpec;


    }

    /**
     * 规格汇总合并
     * @param specList
     * @return
     */
    private Map<String, Set<String>> putAllSpec(List<String> specList) {
        //合并后的Map对象
        Map<String, Set<String>> allSpec = new HashMap<String,Set<String>>();
        //循环SpecList
        for (String spec:specList){
            //将每个json字符串转成map
            Map<String,String> specMap = JSON.parseObject(spec, Map.class);
            //合并流程//循环所有map
            for (Map.Entry<String,String> entry: specMap.entrySet()){
                //取出map 并且获取对应的key 以及对应 value
                String key = entry.getKey();//名字
                String value = entry.getValue(); //规格
                //将循环的数据合并到一个map<String,Set<String>>
                //从allSpec获取当前规格对应的数据
                Set<String> specSet = allSpec.get(key);
                if (specSet==null){
                    //之前allspec前没有该规格
                  specSet = new HashSet<String>();
                }
                specSet.add(value);
                allSpec.put(key,specSet);

            }

        }
        return allSpec;
    }

    /**
     * 品牌分组查询
     * @param nativeSearchQueryBuilder
     * @return
     */
    private List<String> searchBrandList(NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        //添加一个分组查询
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuBrand").field("brandName").size(5000));
        //分页参数-总记录数
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);


        //获取分组数据  .get("SkuCategory"); 获取指域的集合数
        StringTerms stringTerms = aggregatedPage.getAggregations().get("skuBrand");
        List<String> brandyList = new ArrayList<String>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String brandName = bucket.getKeyAsString();  //其中一个分类名字
            brandyList.add(brandName);
        }
        return brandyList;
    }

    /**
     * 分类分组查询
     * @param nativeSearchQueryBuilder
     * @return
     */
    private List<String> searchCategoryList(NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        //添加一个分组查询
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuCategory").field("categoryName").size(5000));
        //分页参数-总记录数
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);


        //获取分组数据  .get("SkuCategory"); 获取指域的集合数
        StringTerms stringTerms = aggregatedPage.getAggregations().get("skuCategory");
        List<String> categoryList = new ArrayList<String>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String categoryName = bucket.getKeyAsString();  //其中一个分类名字
            categoryList.add(categoryName);
        }
        return categoryList;
    }

    /* *
     * 导入索引库
     */
    @Override
    public void importDate() {
        //调用fegin 查询List<Sku>
        Result<List<Sku>> skuResult = null;
        try {
            skuResult = skuFeign.findAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //将List<Sku> 转成List<SkuInfo>
        List<SkuInfo> skuInfoList = JSON.parseArray(JSON.toJSONString(skuResult.getData()),SkuInfo.class);
        //循环当前skuinfolist
        for (SkuInfo skuInfo: skuInfoList){
            //获取spec MapString -》Map类型 {"电视音响效果":"立体声","电视屏幕尺寸":"20英寸","尺码":"165"}
            Map<String, Object> specMap= JSON.parseObject(skuInfo.getSpec()) ;
            skuInfo.setSpecMap(specMap);
        }
        //调用dao实现数据导入
        skuEsMapper.saveAll(skuInfoList);
    }
}
