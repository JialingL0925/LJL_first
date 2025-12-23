package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.model.entity.AccountingSubject;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * 会计科目Mapper
 */
@Repository
public interface AccountingSubjectMapper extends BaseMapper<AccountingSubject> {
    // 新增：根据科目编码查询科目（注解SQL实现，匹配数据库字段）
    @Select("SELECT subject_id AS subjectId, subject_code AS subjectCode, subject_name AS subjectName, " +
            "subject_level AS subjectLevel, parent_subject_id AS parentSubjectId, subject_type AS subjectType, " +
            "status, deactivation_reason AS deactivationReason, create_time AS createTime, update_time AS updateTime " +
            "FROM accounting_subject WHERE subject_code = #{subjectCode}")
    AccountingSubject selectByCode(String subjectCode);
}