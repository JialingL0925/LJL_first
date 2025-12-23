package com.example.demo.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 供应商实体类
 */
@Data // Lombok注解，自动生成get/set/toString
@TableName("sys_supplier") // 关联数据库表名
public class SysSupplier {

    /**
     * 供应商ID（主键）
     */
    @TableId(type = IdType.AUTO) // 主键自增
    private Long id;

    /**
     * 供应商全称
     */
    @TableField("supplier_name")
    private String supplierName;

    /**
     * 联系人
     */
    @TableField("contact_person")
    private String contactPerson;

    /**
     * 联系电话
     */
    @TableField("phone")
    private String phone;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 详细地址
     */
    @TableField("address")
    private String address;

    /**
     * 营业执照编号
     */
    @TableField("business_license")
    private String businessLicense;

    /**
     * 开户银行
     */
    @TableField("bank_name")
    private String bankName;

    /**
     * 银行账号
     */
    @TableField("bank_account")
    private String bankAccount;

    /**
     * 纳税人识别号
     */
    @TableField("tax_id")
    private String taxId;

    /**
     * 状态（1-正常，2-停用）
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建人ID
     */
    @TableField("create_user_id")
    private Long createUserId;

    /**
     * 创建时间（MySQL默认填充）
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间（MyBatis-Plus自动填充）
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}