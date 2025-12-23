package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.annotation.Resource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Resource
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // 注入PasswordConfig中定义的密码编码器
    @Resource
    private PasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 关闭CSRF防护（前后端分离必关）
                .csrf().disable()
                // 无状态Session（JWT模式）
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // 授权规则：开发环境全放行（核心修改）
                .authorizeRequests()
                // 1. 放行跨域预检请求（解决CORS）
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 2. 放行登录接口（保留原有配置，兼容历史逻辑）
                .antMatchers("/login", "/user/login").permitAll()
                // 3. 放行所有接口（包括未来新增的/accounting/supplier、/accounting/xxx等）
                .anyRequest().permitAll()
                .and()
                // 可选：开发环境可注释这行（避免JWT过滤器干扰测试）
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}