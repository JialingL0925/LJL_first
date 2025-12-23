package com.example.demo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.model.dto.PurchaseOrderDTO;
import com.example.demo.model.entity.PurchaseOrder;

import java.util.List;
import java.util.Map;

// 绿色接口文件，对应PurchaseOrderServiceImpl的实现
public interface PurchaseOrderService extends IService<PurchaseOrder> {
    boolean saveOrder(PurchaseOrderDTO dto);
    boolean updateOrder(PurchaseOrderDTO dto);
    boolean auditOrder(Long id, Long userId);
    IPage<PurchaseOrder> pageList(Page<PurchaseOrder> page, String orderNo, Long supplierId, String status, String startTime, String endTime, Long projectId);
    Map<String, Object> getOrderDetail(Long id);
    PurchaseOrder getByOrderNo(String orderNo);
    boolean batchUpdateStatus(List<Long> ids, String status);
}