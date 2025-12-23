package com.example.demo.controller;

import com.example.demo.model.vo.ResultVO;
import com.example.demo.service.EmployeeService;
import com.example.demo.util.ResultUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/user")  // 接口前缀
public class UserController {

    @Resource
    private EmployeeService employeeService;

    // 登录接口（无需认证，公开访问）
    @PostMapping("/login")
    public ResultVO login(
            @RequestParam String phone,  // 手机号（登录账号）
            @RequestParam String password  // 密码（明文）
    ) {
        // 调用Service层登录逻辑
        return employeeService.login(phone, password);
    }

    // 注册接口（无需认证，公开访问）
    @PostMapping("/register")
    public ResultVO register(
            @RequestParam String name,     // 姓名
            @RequestParam String phone,    // 手机号（登录账号）
            @RequestParam String password, // 密码（明文）
            @RequestParam(required = false) String roleIdStr  // 角色ID（字符串格式，前端传递）
    ) {
        try {
            // 转换roleId字符串为Long类型
            Long roleId = null;
            if (roleIdStr != null && !roleIdStr.trim().isEmpty()) {
                try {
                    roleId = Long.parseLong(roleIdStr.trim());
                } catch (NumberFormatException e) {
                    return ResultUtil.error("角色ID格式错误");
                }
            }
            // 调用Service层注册逻辑
            return employeeService.register(name, phone, password, roleId);
        } catch (Exception e) {
            return ResultUtil.systemError("注册失败：" + e.getMessage());
        }
    }
}