package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.model.entity.SalesOrder;
import org.springframework.stereotype.Repository;

/**
 * 销售订单Mapper接口
 */
@Repository
public interface SalesOrderMapper extends BaseMapper<SalesOrder> {
    // 继承BaseMapper已包含CRUD方法
}