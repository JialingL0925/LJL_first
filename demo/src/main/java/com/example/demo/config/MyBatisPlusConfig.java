package com.example.demo.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 合并版MyBatis-Plus配置：
 * 1. 保留原有Mapper扫描功能
 * 2. 新增分页插件配置（解决供应商分页查询失效）
 */
@Configuration
@MapperScan("com.example.demo.mapper")  // 保留你的原有Mapper扫描
public class MyBatisPlusConfig {

    /**
     * 新增分页插件（必须配置，否则Page分页不生效）
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加MySQL分页插件（默认适配MySQL，其他数据库需指定方言）
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }
}