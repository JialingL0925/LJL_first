package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.model.entity.JournalEntry;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public interface JournalEntryMapper extends BaseMapper<JournalEntry> {
    
    /**
     * 测试方法：直接使用原生SQL查询，返回Map（用于调试）
     */
    @Select("SELECT id, status, post_user_id, post_time, create_time FROM journal_entry WHERE id = #{id}")
    Map<String, Object> selectRawById(@Param("id") Long id);
}