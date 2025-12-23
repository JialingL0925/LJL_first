package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.model.entity.Project;
import com.example.demo.mapper.ProjectMapper;
import com.example.demo.service.ProjectService;
import org.springframework.stereotype.Service;

/**
 * 项目Service实现类
 * 重写save、getById和selectProjectPage方法，使用自定义的XML方法确保字段映射正确
 */
@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {
    
    /**
     * 重写save方法，使用自定义的insert方法（在XML中定义）
     * 这样可以确保在 map-underscore-to-camel-case: false 的情况下字段映射正确
     */
    @Override
    public boolean save(Project entity) {
        int result = baseMapper.insert(entity);
        return result > 0;
    }
    
    /**
     * 自定义分页查询方法（使用XML映射，确保字段映射正确）
     */
    @Override
    public IPage<Project> selectProjectPage(Page<Project> page, String projectName, String projectCode, String status) {
        return baseMapper.selectProjectPage(page, projectName, projectCode, status);
    }
    
    /**
     * 重写getById方法，使用自定义的selectById方法（在XML中定义）
     */
    @Override
    public Project getById(java.io.Serializable id) {
        return baseMapper.selectById((Long) id);
    }
    
    /**
     * 重写updateById方法，使用自定义的updateById方法（在XML中定义）
     * 这样可以确保在 map-underscore-to-camel-case: false 的情况下字段映射正确
     */
    @Override
    public boolean updateById(Project entity) {
        int result = baseMapper.updateById(entity);
        return result > 0;
    }
}

