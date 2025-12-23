package com.example.demo.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 收款单实体类
 */
@Data
@TableName("ar_payment") // 关联数据库表名
public class ArPayment {
    /**
     * 收款单ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO) // 使用自增主键
    private Long id;

    /**
     * 收款单编号
     */
    @TableField("payment_no") // 映射到payment_no
    private String payment_no;

    /**
     * 关联应收单ID
     */
    @TableField("receivable_id") // 映射到receivable_id
    private Long receivable_id;

    /**
     * 客户ID
     */
    @TableField("customer_id") // 映射到customer_id
    private Long customer_id;

    /**
     * 客户名称
     */
    @TableField("customer_name") // 映射到customer_name
    private String customer_name;

    /**
     * 收款金额
     */
    @TableField("payment_amount") // 映射到payment_amount
    private BigDecimal payment_amount;

    /**
     * 收款日期
     */
    @TableField("payment_date") // 映射到payment_date
    private Date payment_date;

    /**
     * 收款方式
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