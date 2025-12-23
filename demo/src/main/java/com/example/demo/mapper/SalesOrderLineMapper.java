package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.model.entity.SalesOrderLine;
import org.springframework.stereotype.Repository;

/**
 * 销售订单项Mapper接口
 */
@Repository
public interface SalesOrderLineMapper extends BaseMapper<SalesOrderLine> {
    // 继承BaseMapper已包含CRUD方法
}