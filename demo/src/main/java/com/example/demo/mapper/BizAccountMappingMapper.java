package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.model.entity.BizAccountMapping;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface BizAccountMappingMapper extends BaseMapper<BizAccountMapping> {
    // SQL参数严格匹配数据库字段，无驼峰转换
    @Select("SELECT * FROM biz_account_mapping WHERE biz_type = #{biz_type} AND biz_sub_type = #{biz_sub_type}")
    BizAccountMapping selectByBizTypeAndSubType(@Param("biz_type") String biz_type, @Param("biz_sub_type") String biz_sub_type);
}