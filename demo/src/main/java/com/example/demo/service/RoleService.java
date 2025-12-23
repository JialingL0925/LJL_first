package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.model.entity.Role;

/**
 * 角色服务接口
 * 继承IService后，已经包含了getById等方法，无需重复定义
 */
public interface RoleService extends IService<Role> {
}

