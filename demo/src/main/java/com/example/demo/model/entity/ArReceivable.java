package com.example.demo.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("ar_receivable")
public class ArReceivable {
    @TableId(type = IdType.AUTO)
    private Long id; // 数据库字段：id
    private String receivable_no; // 数据库字段：receivable_no
    private Long order_id; // 数据库字段：order_id
    private Long customer_id; // 数据库字段：customer_id
    private String customer_name; // 数据库字段：customer_name
    private BigDecimal total_amount; // 数据库字段：total_amount
    private String status; // 数据库字段：status
    private Long create_user_id; // 数据库字段：create_user_id
    private Date create_time; // 数据库字段：create_time
    private Date update_time; // 数据库字段：update_time
}