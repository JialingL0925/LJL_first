package com.example.demo.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("journal_entry")
public class JournalEntry {
    @TableId(type = IdType.AUTO)
    private Long id; // 数据库字段：id
    
    @TableField("source_type")
    private String source_type; // 数据库字段：source_type
    
    @TableField("source_id")
    private Long source_id; // 数据库字段：source_id
    
    @TableField("debit_code")
    private String debit_code; // 数据库字段：debit_code
    
    @TableField("debit_name")
    private String debit_name; // 数据库字段：debit_name
    
    @TableField("credit_code")
    private String credit_code; // 数据库字段：credit_code
    
    @TableField("credit_name")
    private String credit_name; // 数据库字段：credit_name
    
    @TableField("amount")
    private BigDecimal amount; // 数据库字段：amount
    
    @TableField("tax_amount")
    private BigDecimal tax_amount; // 数据库字段：tax_amount
    
    @TableField("status")
    private String status; // 数据库字段：status
    
    @TableField("remark")
    private String remark; // 数据库字段：remark
    
    @TableField("create_user_id")
    private Long create_user_id; // 数据库字段：create_user_id
    
    @TableField("create_time")
    private Date create_time; // 数据库字段：create_time
    
    @TableField("update_time")
    private Date update_time; // 数据库字段：update_time
    
    @TableField("post_user_id")
    private Long post_user_id; // 数据库字段：post_user_id 过账人ID
    
    @TableField("post_time")
    private Date post_time; // 数据库字段：post_time 过账时间
}