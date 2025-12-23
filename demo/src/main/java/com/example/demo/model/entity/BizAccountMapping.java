package com.example.demo.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("biz_account_mapping")
public class BizAccountMapping {
    @TableId(type = IdType.AUTO)
    private Long id; // 数据库字段：id
    private String biz_type; // 数据库字段：biz_type
    private String biz_sub_type; // 数据库字段：biz_sub_type
    private String debit_code; // 数据库字段：debit_code
    private String credit_code; // 数据库字段：credit_code
    private String remark; // 数据库字段：remark
    private Date create_time; // 数据库字段：create_time
    private Date update_time; // 数据库字段：update_time
}