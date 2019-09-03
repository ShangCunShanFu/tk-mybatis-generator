package com.hgd.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**************************************
 * Copyright (C), Navinfo
 * Package: 
 * @Author: 尚村山夫
 * @Date: Created in 2019/8/5 11:00
 * @Description:
 **************************************/
public class CharsetUtil {

    /**
     * 判断字符串s的编码格式<br/>
     * 该方法对于英文字符的编码并不能做出很好的判断<br/>
     *
     * @param s 待判断字符串
     * @return 字符串s编码格式
     */
    public static Charset getStandarCharsetsOf(String s) {
        Charset charset;

        if (s.equals(new String(s.getBytes(StandardCharsets.UTF_8)))) {
            charset = StandardCharsets.UTF_8;
        } else if (s.equals(new String(s.getBytes(StandardCharsets.ISO_8859_1)))) {
            charset = StandardCharsets.ISO_8859_1;
        } else if (s.equals(new String(s.getBytes(StandardCharsets.UTF_16BE)))) {
            charset = StandardCharsets.UTF_16BE;
        } else if (s.equals(new String(s.getBytes(StandardCharsets.UTF_16LE)))) {
            charset = StandardCharsets.UTF_16LE;
        } else if (s.equals(new String(s.getBytes(StandardCharsets.UTF_16)))) {
            charset = StandardCharsets.UTF_16;
        } else {
            charset = StandardCharsets.UTF_8;
        }

        return charset;
    }

}
