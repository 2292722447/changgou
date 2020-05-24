package com.itheima;

import com.github.wxpay.sdk.WXPayUtil;
import org.junit.Test;

public class WeixinUtilTest {



    /*
    生成随机字符
     */
    @Test
    public  void testDemo(){
        String str = WXPayUtil.generateNonceStr();
        System.out.printf(str);


    }

}
