package com.example.demo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.model.dto.SalesOrderDTO;
import com.example.demo.model.entity.SalesOrder;

import java.util.List;
import java.util.Map;

public interface SalesOrderService extends IService<SalesOrder> {
    boolean saveOrder(SalesOrderDTO dto);
    boolean updateOrder(SalesOrderDTO dto);
    boolean auditOrder(Long id, Long userId);
    IPage<SalesOrder> pageList(Page<SalesOrder> page, String orderNo, String customerName, String orderType, String status, String startTime, String endTime, Long projectId);
    Map<String, Object> getOrderDetail(Long id);
    SalesOrder getByOrderNo(String orderNo);
    boolean batchUpdateStatus(List<Long> ids, String status);
}