package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 跨域配置：支持开发和生产环境
        // 注意：setAllowCredentials(true)时不能使用通配符"*"，需要明确指定域名
        try {
            // Spring Boot 2.4+ 推荐写法（支持通配符模式）
            // 允许所有来源（包括开发和生产环境）
            config.addAllowedOriginPattern("*");
        } catch (NoSuchMethodError e) {
            // 兼容Spring Boot 2.3及以下（不支持通配符模式）
            config.addAllowedOrigin("*");
        }
        
        // 注意：如果使用通配符"*"，setAllowCredentials必须为false
        // 如果需要携带凭证，需要明确指定允许的域名
        config.setAllowCredentials(false);  // 使用通配符时设为false
        config.addAllowedMethod("*");      // 允许所有请求方法
        config.addAllowedHeader("*");      // 允许所有请求头
        config.setMaxAge(3600L);           // 跨域缓存1小时

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 所有接口生效
        return new CorsFilter(source);
    }
}