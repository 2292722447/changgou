package com.changgou.oauth.interceptor;

import com.changgou.oauth.util.AdminToken;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TokenRequestInterceptor implements RequestInterceptor {


    /**
     * fegin执行之前进行拦截
     * @param template
     */
    @Override
    public void apply(RequestTemplate template) {

        /**
         *  //从数据库加载查询用户信息
         *         //没有令牌 生成令牌
         *         //令牌需要携带过去
         *         //令牌需要存放到header文件中
         *         //请求 feign调用  拦截器RequestInterceptor = feign调用之前执行拦截
         */

        //生成令牌
        String token = AdminToken.adminToken();
        template.header("Authorization","bearer "+token);

    }
}
