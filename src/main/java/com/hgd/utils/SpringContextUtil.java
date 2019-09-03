package com.hgd.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author ：尚村山夫
 * @date ：Created in 2019/8/4 15:22
 * @modified By：
 */
@Component
public class SpringContextUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.applicationContext = applicationContext;
    }

    public static Object getBeanByName(String beanName){
        return applicationContext.getBean(beanName);
    }

    public static <T> T getBeanByType(Class<T> clazz){
        return applicationContext.getBean(clazz);
    }
}
