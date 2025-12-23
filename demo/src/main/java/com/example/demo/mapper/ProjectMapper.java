package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.model.entity.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 项目Mapper（继承BaseMapper，与实体类绑定）
 * 使用自定义XML映射确保字段映射正确（map-underscore-to-camel-case: false）
 */
@Mapper
public interface ProjectMapper extends BaseMapper<Project> {
    /**
     * 自定义插入方法（在XML中定义，确保字段映射正确）
     */
    int insert(Project project);
    
    /**
     * 自定义分页查询方法（在XML中定义，确保字段映射正确）
     */
    IPage<Project> selectProjectPage(Page<Project> page, 
                                      @Param("projectName") String projectName, 
                                      @Param("projectCode") String projectCode, 
                                      @Param("status") String status);
    
    /**
     * 根据ID查询项目详情（在XML中定义，确保字段映射正确）
     */
    Project selectById(@Param("projectId") Long projectId);
    
    /**
     * 根据ID更新项目（在XML中定义，确保字段映射正确）
     */
    int updateById(Project project);
}

