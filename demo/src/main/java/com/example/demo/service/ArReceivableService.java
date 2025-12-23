package com.example.demo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.model.entity.ArReceivable;

/**
 * 应收款单服务接口
 */
public interface ArReceivableService extends IService<ArReceivable> {
    /**
     * 分页查询应收单列表
     */
    IPage<ArReceivable> pageList(Page<ArReceivable> page, String receivableNo, Long orderId, Long customerId, String status, String createTimeStart, String createTimeEnd);
    
    /**
     * 统计未收款订单数量（状态为"待确认"或"已确认"）
     */
    Long countUnpaidReceivables();
}