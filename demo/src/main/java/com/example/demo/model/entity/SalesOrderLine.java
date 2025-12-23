package com.example.demo.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("sales_order_line")
public class SalesOrderLine {
    @TableId(type = IdType.AUTO)
    private Long id; // 数据库字段：id
    private Long order_id; // 数据库字段：order_id
    private String product_code; // 数据库字段：product_code
    private String product_name; // 数据库字段：product_name
    private String spec; // 数据库字段：spec
    private String unit; // 数据库字段：unit
    private BigDecimal quantity; // 数据库字段：quantity
    private BigDecimal price; // 数据库字段：price
    private BigDecimal amount; // 数据库字段：amount
    private BigDecimal tax_rate; // 数据库字段：tax_rate
    private BigDecimal tax_amount; // 数据库字段：tax_amount
    private Date create_time; // 数据库字段：create_time
    private Date update_time; // 数据库字段：update_time
}