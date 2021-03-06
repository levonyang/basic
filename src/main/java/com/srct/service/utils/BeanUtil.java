/**
 * Project Name:SpringBootCommon
 * File Name:BeanUtil.java
 * Package Name:com.srct.service.utils
 * Date:2018年4月26日上午11:17:45
 * Copyright (c) 2018, ruopeng.sha All Rights Reserved.
 */
package com.srct.service.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.*;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * ClassName:BeanUtil <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON. <br/>
 * Date: 2018年4月26日 上午11:17:45 <br/>
 *
 * @author ruopeng.sha
 * @version
 * @see
 * @since JDK 1.8
 */


/**
 * 工具类-spring bean
 */
@Slf4j
@Configuration
public class BeanUtil implements ApplicationContextAware, ApplicationListener<ApplicationEvent> {

    private static ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();

    private static BeanNameGenerator beanNameGenerator = new AnnotationBeanNameGenerator();

    private static WebApplicationContext ctx = null;
    /**
     *
     */
    private static Map<String, BeanCopier> beanCopierMap = new HashMap<String, BeanCopier>();

    /**
     * 根据bean名称获取
     *
     * @param name
     * @return
     */
    public static Object getBean(String name) {
        if (ctx == null) {
            log.info("Call BeanUtils too early");
            return null;
        }
        return ctx.getBean(name);
    }

    /**
     * 根据bean Class获取
     *
     * @param clazz
     * @return
     */
    public static Object getBean(Class<?> clazz) {
        if (ctx == null) {
            log.info("Call BeanUtils too early");
            return null;
        }
        return ctx.getBean(clazz);
    }

    public static void registerBean(String name, Class<?> beanClass) {
        DefaultListableBeanFactory registry = (DefaultListableBeanFactory) ctx.getAutowireCapableBeanFactory();
        AnnotatedGenericBeanDefinition abd = new AnnotatedGenericBeanDefinition(beanClass);
        ScopeMetadata scopeMetadata = scopeMetadataResolver.resolveScopeMetadata(abd);
        abd.setScope(scopeMetadata.getScopeName());
        // 可以自动生成name
        String beanName = (name != null ? name : beanNameGenerator.generateBeanName(abd, registry));
        AnnotationConfigUtils.processCommonDefinitionAnnotations(abd);
        BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(abd, beanName);
        BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, registry);
    }

    public static String getProperty(String name) {
        return ctx.getEnvironment().getProperty(name);
    }

    public static Object getProperty(String name, Class<?> clazz) {
        return ctx.getEnvironment().getProperty(name, clazz);
    }

    public static String[] getBeanNamesForType(Class<?> clazz) {
        return ctx.getBeanNamesForType(clazz);
    }

    /// 获取当前环境
    public static String getActiveProfile() {
        return ctx.getEnvironment().getActiveProfiles()[0];
    }

    public static <T> T copyProperties(Object source, Class<T> targetClazz) {
        try {
            T target = targetClazz.newInstance();
            copyProperties(source, target);
            return target;
        } catch (InstantiationException | IllegalAccessException e) {
            return null;
        }
    }

    /**
     * @param source 资源类
     * @param target 目标类
     * @Title: copyProperties
     * @Description: bean属性转换
     * @author ruopeng.sha
     * @date 2018年11月8日下午11:41
     */
    public static void copyProperties(Object source, Object target) {
        if (source == null) {
            return;
        }
        String beanKey = generateKey(source.getClass(), target.getClass());
        BeanCopier copier = null;
        if (!beanCopierMap.containsKey(beanKey)) {
            copier = BeanCopier.create(source.getClass(), target.getClass(), false);
            beanCopierMap.put(beanKey, copier);
        } else {
            copier = beanCopierMap.get(beanKey);
        }
        copier.copy(source, target, null);
    }

    private static String generateKey(Class<?> class1, Class<?> class2) {
        return class1.toString() + class2.toString();
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        log.info("onApplicationEvent {}", event);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        log.info("setApplicationContext");
        ctx = (WebApplicationContext) applicationContext;
    }
    /*
     * 注： (1)相同属性名，且类型不匹配时候的处理，ok，但是未满足的属性不拷贝；
     * (2)get和set方法不匹配的处理，创建拷贝的时候报错，无法拷贝任何属性(当且仅当sourceClass的get方法超过set方法时出现)
     * (3)BeanCopier 初始化例子：BeanCopier copier = BeanCopier.create(Source.class,
     * Target.class, useConverter=true)
     * 第三个参数userConverter,是否开启Convert,默认BeanCopier只会做同名，同类型属性的copier,否则就会报错.
     * copier = BeanCopier.create(source.getClass(), target.getClass(), false);
     * copier.copy(source, target, null); (4)修复beanCopier对set方法强限制的约束
     * 改写net.sf.cglib.beans.BeanCopier.Generator.generateClass(ClassVisitor)方法
     * 将133行的 MethodInfo write =
     * ReflectUtils.getMethodInfo(setter.getWriteMethod()); 预先存一个names2放入 109
     * Map names2 = new HashMap(); 110 for (int i = 0; i < getters.length; ++i)
     * { 111 names2.put(setters[i].getName(), getters[i]); }
     * 调用这行代码前判断查询下，如果没有改writeMethod则忽略掉该字段的操作，这样就可以避免异常的发生。
     */
}
