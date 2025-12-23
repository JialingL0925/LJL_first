package com.example.demo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.model.entity.ApPayable;

import java.util.List;

public interface ApPayableService extends IService<ApPayable> {
    IPage<ApPayable> pageList(Page<ApPayable> page, String payableNo, Long orderId, Long supplierId, Integer status, String payableDateStart, String payableDateEnd);
    List<ApPayable> getByOrderId(Long orderId);
    List<ApPayable> getBySupplierId(Long supplierId);
    boolean batchUpdateStatus(List<Long> ids, Integer status);
    // 统计未付款订单数量（状态为"待确认"或"已确认"）
    Long countUnpaidPayables();
}