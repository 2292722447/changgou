package com.changgou.order.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;

@Configuration
public class TokenRequestInterceptor implements RequestInterceptor {


    /**
     * fegin执行之前进行拦截
     * @param template
     */
    @Override
    public void apply(RequestTemplate template) {

        /**
         * 获取用户令牌
         * 将令牌封装到头文件中
         */

        //记录了当前用户请求的所有数据
        ServletRequestAttributes requestAttributes =(ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        //获取请求头数据  获取所有头的名字
        Enumeration<String> headerNames = requestAttributes.getRequest().getHeaderNames();
        while (headerNames.hasMoreElements()){
            //请求头的key
            String headerKey = headerNames.nextElement();
            //获取请求头的value
            String headerValue = requestAttributes.getRequest().getHeader(headerKey);
            System.out.printf(headerKey+":"+headerValue);

            //请求头信息封装到头中 使用feign调用时 会传递给下个微服务
            template.header(headerKey,headerValue);
        }
    }
}
