package com.example.demo.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("role")
public class Role {
    @TableId(value = "role_id", type = IdType.AUTO)
    private Long roleId;      // 角色ID
    
    @TableField("role_name")
    private String roleName;  // 角色名称
}