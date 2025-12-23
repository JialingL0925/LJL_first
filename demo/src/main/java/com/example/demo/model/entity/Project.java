package com.example.demo.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 项目实体类
 */
@Data
@TableName("project")
public class Project {
    
    /**
     * 项目ID（主键）
     */
    @TableId(type = IdType.AUTO)
    @TableField("project_id")
    private Long projectId;
    
    /**
     * 项目编号（唯一）
     */
    @TableField("project_code")
    private String projectCode;
    
    /**
     * 项目名称
     */
    @TableField("project_name")
    private String projectName;
    
    /**
     * 项目经理/负责人
     */
    @TableField("project_manager")
    private String projectManager;
    
    /**
     * 开始日期
     */
    @TableField("start_date")
    private Date startDate;
    
    /**
     * 结束日期
     */
    @TableField("end_date")
    private Date endDate;
    
    /**
     * 预算金额
     */
    @TableField("budget")
    private BigDecimal budget;
    
    /**
     * 状态（进行中/已完成/已暂停/已取消）
     */
    @TableField("status")
    private String status;
    
    /**
     * 项目描述
     */
    @TableField("description")
    private String description;
    
    /**
     * 创建人ID
     */
    @TableField("create_user_id")
    private Long createUserId;
    
    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;
    
    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;
}

