package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.model.entity.SysSupplier;
import org.apache.ibatis.annotations.Mapper;

/**
 * 供应商Mapper接口
 */
@Mapper // 标记为MyBatis的Mapper
public interface SysSupplierMapper extends BaseMapper<SysSupplier> {
    // 继承BaseMapper后，自动拥有增删改查方法
}