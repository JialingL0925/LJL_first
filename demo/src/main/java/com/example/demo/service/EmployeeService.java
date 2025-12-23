package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.model.entity.Employee;
import com.example.demo.model.vo.ResultVO;
import org.springframework.security.core.userdetails.UserDetailsService;

// 继承IService（MyBatis-Plus基础CRUD）和UserDetailsService（Security用户加载）
public interface EmployeeService extends IService<Employee>, UserDetailsService {
    // 登录方法（手机号+密码）
    ResultVO login(String phone, String password);
    
    // 注册方法（姓名+手机号+密码+角色ID）
    ResultVO register(String name, String phone, String password, Long roleId);
}