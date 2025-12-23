package com.example.demo.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 付款单实体类
 */
@Data
@TableName("ap_payment") // 关联数据库表名
public class ApPayment {
    /**
     * 付款单ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO) // 使用自增主键
    private Long id;

    /**
     * 付款单编号
     */
    @TableField("payment_no") // 映射到payment_no
    private String payment_no;

    /**
     * 关联应付单ID
     */
    @TableField("payable_id") // 映射到payable_id
    private Long payable_id;

    /**
     * 供应商ID
     */
    @TableField("supplier_id") // 映射到supplier_id
    private Long supplier_id;

    /**
     * 供应商名称
     */
    @TableField("supplier_name") // 映射到supplier_name
    private String supplier_name;

    /**
     * 付款金额
     */
    @TableField("payment_amount") // 映射到payment_amount
    private BigDecimal payment_amount;

    /**
     * 付款日期
     */
    @TableField("payment_date") // 映射到payment_date
    private Date payment_date;

    /**
     * 付款方式
     */
    @TableField("payment_method") // 映射到payment_method
    private String payment_method;

    /**
     * 状态
     */
    @TableField("status") // 映射到status
    private String status;

    /**
     * 创建人ID
     */
    @TableField("create_user_id") // 映射到create_user_id
    private Long create_user_id;

    /**
     * 创建时间
     */
    @TableField("create_time") // 映射到create_time
    private Date create_time;

    /**
     * 更新时间
     */
    @TableField("update_time") // 映射到update_time
    private Date update_time;
}