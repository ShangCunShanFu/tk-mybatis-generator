package com.hgd.utils;

import org.springframework.core.env.Environment;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author ：尚村山夫
 * @date ：Created in 2019/8/4 15:27
 * @modified By：
 */
public class EnvironmentUtil {

    /**
     * 从environment环境中获取相应的属性
     * @param environment 容器环境
     * @param key 属性key
     * @return 与key对应的属性
     */
    public static String getProperty(Environment environment, String key) {
        String property = environment.getProperty(key);

        if(null != property){
            Charset standarCharsetsOf = CharsetUtil.getStandarCharsetsOf(property);
            return new String(property.getBytes(standarCharsetsOf), StandardCharsets.UTF_8);
        }

        return null;
    }
}
