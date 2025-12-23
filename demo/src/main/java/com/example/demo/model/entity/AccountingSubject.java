package com.example.demo.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 会计科目实体类（手动指定字段映射，无需驼峰转换）
 */
@Data
@TableName("accounting_subject") // 关联数据库表名
public class AccountingSubject {
    /**
     * 科目ID（主键）
     */
    @TableId(value = "subject_id", type = IdType.ASSIGN_ID) // 直接在@TableId指定数据库字段名
    private Long subjectId;

    /**
     * 科目编码
     */
    @TableField("subject_code") // 映射到subject_code
    private String subjectCode;

    /**
     * 科目名称
     */
    @TableField("subject_name") // 映射到subject_name
    private String subjectName;

    /**
     * 科目级别
     */
    @TableField("subject_level") // 映射到subject_level
    private Integer subjectLevel;

    /**
     * 父科目ID
     */
    @TableField("parent_subject_id") // 映射到parent_subject_id
    private Long parentSubjectId;

    /**
     * 科目类型（资产/负债/所有者权益/成本/损益等）
     */
    @TableField("subject_type") // 映射到subject_type
    private String subjectType;

    /**
     * 状态（启用/停用）
     */
    @TableField("status") // 数据库字段是status，无需转换
    private String status;

    /**
     * 停用原因
     */
    @TableField("deactivation_reason") // 映射到deactivation_reason
    private String deactivationReason;

    /**
     * 创建时间
     */
    @TableField("create_time") // 映射到create_time
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("update_time") // 映射到update_time
    private Date updateTime;
}