package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.model.entity.ArReceivable;
import org.springframework.stereotype.Repository;

/**
 * 应收款单Mapper接口
 */
@Repository
public interface ArReceivableMapper extends BaseMapper<ArReceivable> {
    // 继承BaseMapper已包含CRUD方法
}