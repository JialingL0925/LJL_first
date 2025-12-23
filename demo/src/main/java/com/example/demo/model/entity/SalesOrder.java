package com.example.demo.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("sales_order")
public class SalesOrder {
    @TableId(type = IdType.AUTO)
    private Long id; // 数据库字段：id
    private String order_no; // 数据库字段：order_no
    private Long customer_id; // 数据库字段：customer_id
    private String customer_name; // 数据库字段：customer_name
    private String biz_sub_type; // 数据库字段：biz_sub_type
    private BigDecimal total_amount; // 数据库字段：total_amount
    private String status; // 数据库字段：status
    private Long create_user_id; // 数据库字段：create_user_id
    private Long project_id; // 数据库字段：project_id（关联项目ID，可选）
    private String project_name; // 数据库字段：project_name（项目名称）
    private Date create_time; // 数据库字段：create_time
    private Date update_time; // 数据库字段：update_time
}