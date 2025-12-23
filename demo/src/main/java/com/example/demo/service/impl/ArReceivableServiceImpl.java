package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.mapper.ArReceivableMapper;
import com.example.demo.model.entity.ArReceivable;
import com.example.demo.service.ArReceivableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 应收款单服务实现类
 */
@Slf4j
@Service
public class ArReceivableServiceImpl extends ServiceImpl<ArReceivableMapper, ArReceivable> implements ArReceivableService {

    /**
     * 分页查询应收单列表
     */
    @Override
    public IPage<ArReceivable> pageList(Page<ArReceivable> page, String receivableNo, Long orderId, Long customerId, String status, String createTimeStart, String createTimeEnd) {
        log.info("分页查询应收单列表, pageNum={}, pageSize={}, receivableNo={}, orderId={}, customerId={}, status={}, createTimeStart={}, createTimeEnd={}",
                page.getCurrent(), page.getSize(), receivableNo, orderId, customerId, status, createTimeStart, createTimeEnd);
        try {
            QueryWrapper<ArReceivable> queryWrapper = new QueryWrapper<>();
            
            // 应收单号模糊查询
            if (receivableNo != null && !receivableNo.isEmpty()) {
                queryWrapper.like("receivable_no", receivableNo);
            }
            
            // 订单ID精确查询
            if (orderId != null) {
                queryWrapper.eq("order_id", orderId);
            }
            
            // 客户ID精确查询
            if (customerId != null) {
                queryWrapper.eq("customer_id", customerId);
            }
            
            // 状态精确查询
            if (status != null && !status.isEmpty()) {
                queryWrapper.eq("status", status);
            }
            
            // 创建时间范围查询
            if (createTimeStart != null && !createTimeStart.isEmpty()) {
                queryWrapper.ge("create_time", createTimeStart);
            }
            if (createTimeEnd != null && !createTimeEnd.isEmpty()) {
                queryWrapper.le("create_time", createTimeEnd);
            }
            
            // 优先显示未收款订单：按状态排序（待确认 > 已确认 > 已核销 > 已取消）
            // 使用CASE WHEN实现自定义排序
            queryWrapper.last("ORDER BY CASE status " +
                    "WHEN '待确认' THEN 1 " +
                    "WHEN '已确认' THEN 2 " +
                    "WHEN '已核销' THEN 3 " +
                    "WHEN '已取消' THEN 4 " +
                    "ELSE 5 END ASC, create_time DESC");
            
            IPage<ArReceivable> pageResult = this.page(page, queryWrapper);
            log.info("分页查询应收单列表成功, 总数: {}", pageResult.getTotal());
            return pageResult;
        } catch (Exception e) {
            log.error("分页查询应收单列表异常", e);
            throw e;
        }
    }

    @Override
    public Long countUnpaidReceivables() {
        LambdaQueryWrapper<ArReceivable> queryWrapper = new LambdaQueryWrapper<>();
        // 统计状态为"待确认"或"已确认"的订单（未收款）
        queryWrapper.in(ArReceivable::getStatus, "待确认", "已确认");
        return this.count(queryWrapper);
    }
}