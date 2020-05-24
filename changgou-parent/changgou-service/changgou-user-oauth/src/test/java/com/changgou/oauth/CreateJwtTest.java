package com.changgou.oauth;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.rsa.crypto.KeyStoreKeyFactory;

import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 令牌的创建和解析
 */
public class CreateJwtTest {
    /**
     * 创建令牌
     */
    @Test
    public void  testToken(){
        //加载证书
        ClassPathResource resource = new ClassPathResource("changgou.jks");
        //获取证书
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource,"changgou".toCharArray());
        //获取证书中的一对秘钥
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair("changgou","changgou".toCharArray());
        //获取私钥
        RSAPrivateKey privateKey =(RSAPrivateKey) keyPair.getPrivate();
        //创建令牌
        Map<String, Object> payload = new HashMap<>();
        payload.put("nikenmae","tomcat");
        payload.put("address","sz");
        payload.put("authorities",new String[] {"admain","oauth"});
        Jwt jwt = JwtHelper.encode(JSON.toJSONString(payload), new RsaSigner(privateKey));
        //获取令牌数据
        String token = jwt.getEncoded();
        System.out.printf(token);
    }

    /**
     * 解析令牌
     */
    @Test
    public void testParse(){
       String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJuaWtlbm1hZSI6InRvbWNhdCIsImFkZHJlc3MiOiJzeiIsImF1dGhvcml0aWVzIjpbImFkbWFpbiIsIm9hdXRoIl19.YdLQK73fIdJSnfSAW9KfifYsx4I0AXXEigJVlARury6e_xvXhwqzT9IOLd7WO6mQvjsA_7_aIKMxQ2Y2RLX1qXQHEhXSDb5CZFNemG92cBB7317ikO6VX3XEIGvfpwA8SyxGFAf8g3frNu70atPXjEPMXvvigBC9Gh9bHkyMFhtuiy8UAhEQKN30DgeEUahtDFqNvwDS9JCciAOc3JM2RcCG_yThpINExgzv2znM0YOLaNnxjLOxZwoV0DmQxFi_JgmrqJjw5nII8yDouWdzz2yvChf2IsuKe_GSkgTQfUOKu-b3LQTw__Qgr2Iln7Vwc7sJxWAEWePSysTq39-RKw";
        //String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZSI6WyJhcHAiXSwibmFtZSI6bnVsbCwiaWQiOm51bGwsImV4cCI6MjAyMTQxOTQzNCwiYXV0aG9yaXRpZXMiOlsidmlwIiwiYWRtaW4iLCJ1c2VyIl0sImp0aSI6IjE2NGI0MDVlLTlmNTUtNDRhZS1hODNlLWQ1ZGQzY2U4MTY5ZCIsImNsaWVudF9pZCI6ImNoYW5nZ291IiwidXNlcm5hbWUiOiJjaGFuZ2dvdSJ9.nkQSGi56ofD6p34MuIcUANCHo_RZuBOuvtEJE-83YvEiYlJ0FxkgU7SjljjlNc2bC84F27JSpnWADh5aKSikJGohNuzv7R4k1XwolVkVs9wvBRCF6ck8iUcIWNR_k7XJKREpQH6pVZyMUdbudwPOTnimtszB8x0Yx_Dc3wcPybKBGV-KLrmRs8b9HETvXCkE41b0YLyOkWkUsEIIb_E-IlzJ9jrNRUV91y0ObNbPMiH9RixiEvL7Y8QblCHWP7M9Nfc9gVpD1zFGPAuauj8BzDzlZi0cBEfvguQf4erxEanqmpDOABQNFXx9DtioCwkkqYrQ-BaDQhzJ7kcqhaluZA";
        String pulickey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvFsEiaLvij9C1Mz+oyAmt47whAaRkRu/8kePM+X8760UGU0RMwGti6Z9y3LQ0RvK6I0brXmbGB/RsN38PVnhcP8ZfxGUH26kX0RK+tlrxcrG+HkPYOH4XPAL8Q1lu1n9x3tLcIPxq8ZZtuIyKYEmoLKyMsvTviG5flTpDprT25unWgE4md1kthRWXOnfWHATVY7Y/r4obiOL1mS5bEa/iNKotQNnvIAKtjBM4RlIDWMa6dmz+lHtLtqDD2LF1qwoiSIHI75LQZ/CNYaHCfZSxtOydpNKq8eb1/PGiLNolD4La2zf0/1dlcr5mkesV570NxRmU1tFm8Zd3MZlZmyv9QIDAQAB-----END PUBLIC KEY-----";
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(pulickey));
        String claims = jwt.getClaims();
        System.out.printf(claims);

    }

    @Test
    public  void test64(){

        String str = "Y2hhbmdnb3U6Y2hhbmdnb3U=";
        byte[] decode = Base64.getDecoder().decode(str);
        try {
            String decodestr = new String(decode,"UTF-8");
            System.out.printf(decodestr);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

}
