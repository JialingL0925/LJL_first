package com.example.demo.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("ap_payable")
public class ApPayable {
    @TableId(type = IdType.AUTO)
    private Long id; // 数据库字段：id
    private String payable_no; // 数据库字段：payable_no
    private Long order_id; // 数据库字段：order_id
    private Long supplier_id; // 数据库字段：supplier_id
    private String supplier_name; // 数据库字段：supplier_name
    private BigDecimal total_amount; // 数据库字段：total_amount
    private String status; // 数据库字段：status
    private Long create_user_id; // 数据库字段：create_user_id
    private Date create_time; // 数据库字段：create_time
    private Date update_time; // 数据库字段：update_time
}
