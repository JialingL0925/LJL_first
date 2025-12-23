package com.example.demo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.model.entity.Project;

/**
 * 项目Service接口
 */
public interface ProjectService extends IService<Project> {
    /**
     * 自定义分页查询方法（使用XML映射，确保字段映射正确）
     */
    IPage<Project> selectProjectPage(Page<Project> page, String projectName, String projectCode, String status);
}

