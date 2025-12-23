package com.example.demo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.model.entity.ApPayment;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 付款单Service接口
 */
public interface ApPaymentService extends IService<ApPayment> {
    /**
     * 分页查询付款单列表
     */
    IPage<ApPayment> pageList(Page<ApPayment> page, String paymentNo, Long payableId, Long supplierId, String status, String paymentDateStart, String paymentDateEnd);
    
    /**
     * 根据ID查询付款单详情
     */
    ApPayment getById(Long id);
    
    /**
     * 创建付款单
     */
    boolean createPayment(ApPayment payment);
    
    /**
     * 确认付款单
     */
    boolean confirmPayment(Long id, Long userId);
    
    /**
     * 取消付款单
     */
    boolean cancelPayment(Long id);
}