package com.example.demo.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("employee")
public class Employee {
    @TableId(type = IdType.AUTO)
    @TableField("employee_id")
    private Long employeeId;  // 员工ID
    
    @TableField("name")
    private String name;      // 姓名
    
    @TableField("phone")
    private String phone;     // 手机号（登录账号）
    
    @TableField("password")
    private String password;  // 加密后的密码
    
    @TableField("status")
    private String status;    // 状态（在职/停用）
    
    @TableField("role_id")
    private Long roleId;      // 角色ID
}
