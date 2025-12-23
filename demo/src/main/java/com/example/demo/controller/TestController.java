package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 兜底测试Controller（验证Spring扫描/路径映射是否正常）
 */
@RestController
@RequestMapping("/test") // 测试前缀
public class TestController {

    // 测试接口1：简单返回字符串
    @GetMapping("/hello")
    public String hello() {
        return "success";
    }

    // 测试接口2：模拟供应商列表路径（验证/accounting/supplier前缀是否被拦截）
    @GetMapping("/accounting/supplier/test")
    public String supplierTest() {
        return "supplier path success";
    }
}
