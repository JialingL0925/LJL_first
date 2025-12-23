package com.example.demo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.model.entity.ArPayment;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 收款单Service接口
 */
public interface ArPaymentService extends IService<ArPayment> {
    /**
     * 分页查询收款单列表
     */
    IPage<ArPayment> pageList(Page<ArPayment> page, String paymentNo, Long receivableId, Long customerId, String status, String paymentDateStart, String paymentDateEnd);
    
    /**
     * 根据ID查询收款单详情
     */
    ArPayment getById(Long id);
    
    /**
     * 创建收款单
     */
    boolean createPayment(ArPayment payment);
    
    /**
     * 确认收款单
     */
    boolean confirmPayment(Long id, Long userId);
    
    /**
     * 取消收款单
     */
    boolean cancelPayment(Long id);
}