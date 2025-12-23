package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.model.entity.PurchaseOrderLine;
import org.springframework.stereotype.Repository;

/**
 * 采购订单项Mapper（匹配数据库表purchase_order_line）
 * 无驼峰转换，字段名严格匹配数据库下划线格式
 */
@Repository
public interface PurchaseOrderLineMapper extends BaseMapper<PurchaseOrderLine> {
    // 无需自定义方法，BaseMapper已包含CRUD
    // 如需扩展，SQL参数名需严格匹配数据库字段（如order_id、material_code等）
}