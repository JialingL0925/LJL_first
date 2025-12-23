package com.example.demo.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date; // 用java.util.Date适配低版本MySQL datetime

/**
 * 客户实体类（与数据库字段1:1匹配）
 */
@Data
@TableName("sys_customer") // 表名严格匹配
public class SysCustomer {

    /**
     * 客户ID（主键，与数据库customerId完全一致）
     */
    @TableId(type = IdType.AUTO)
    private Long customerId;

    /**
     * 电话（与数据库phone字段匹配）
     */
    @TableField("phone")
    private String phone;

    /**
     * 客户名称（非空，与数据库customerName匹配）
     */
    @TableField("customerName")
    private String customerName;

    /**
     * 邮箱（与数据库email字段匹配）
     */
    @TableField("email")
    private String email;

    /**
     * 地址（与数据库address字段匹配）
     */
    @TableField("address")
    private String address;

    /**
     * 注册日期（与数据库registrationDate匹配）
     */
    @TableField("registrationDate")
    private Date registrationDate;

    /**
     * 备注（与数据库remarks字段匹配）
     */
    @TableField("remarks")
    private String remarks;

    /**
     * 状态（非空，与数据库status字段匹配）
     */
    @TableField("status")
    private String status;

    /**
     * 创建时间（后端代码填充，与数据库createTime匹配）
     */
    @TableField("createTime")
    private Date createTime;

    /**
     * 更新时间（后端代码填充，与数据库updateTime匹配）
     */
    @TableField("updateTime")
    private Date updateTime;
}