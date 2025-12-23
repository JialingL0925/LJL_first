package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.model.entity.ApPayable;
import com.example.demo.mapper.ApPayableMapper;
import com.example.demo.service.ApPayableService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApPayableServiceImpl extends ServiceImpl<ApPayableMapper, ApPayable> implements ApPayableService {

    @Override
    public IPage<ApPayable> pageList(Page<ApPayable> page, String payableNo, Long orderId, Long supplierId, Integer status, String payableDateStart, String payableDateEnd) {
        LambdaQueryWrapper<ApPayable> queryWrapper = new LambdaQueryWrapper<>();
        if (payableNo != null && !payableNo.isEmpty()) {
            queryWrapper.like(ApPayable::getPayable_no, payableNo);
        }
        if (orderId != null) {
            queryWrapper.eq(ApPayable::getOrder_id, orderId);
        }
        if (supplierId != null) {
            queryWrapper.eq(ApPayable::getSupplier_id, supplierId);
        }
        if (status != null) {
            queryWrapper.eq(ApPayable::getStatus, status.toString());
        }
        // 优先显示未付款订单：按状态排序（待确认 > 已确认 > 已核销 > 已取消），然后按创建时间倒序
        // 使用CASE WHEN实现自定义排序
        queryWrapper.last("ORDER BY CASE status " +
                "WHEN '待确认' THEN 1 " +
                "WHEN '已确认' THEN 2 " +
                "WHEN '已核销' THEN 3 " +
                "WHEN '已取消' THEN 4 " +
                "ELSE 5 END ASC, create_time DESC");
        return this.page(page, queryWrapper);
    }

    @Override
    public Long countUnpaidPayables() {
        LambdaQueryWrapper<ApPayable> queryWrapper = new LambdaQueryWrapper<>();
        // 统计状态为"待确认"或"已确认"的订单（未付款）
        queryWrapper.in(ApPayable::getStatus, "待确认", "已确认");
        return this.count(queryWrapper);
    }

    @Override
    public List<ApPayable> getByOrderId(Long orderId) {
        LambdaQueryWrapper<ApPayable> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApPayable::getOrder_id, orderId);
        return this.list(queryWrapper);
    }

    @Override
    public List<ApPayable> getBySupplierId(Long supplierId) {
        LambdaQueryWrapper<ApPayable> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApPayable::getSupplier_id, supplierId);
        return this.list(queryWrapper);
    }

    @Override
    public boolean batchUpdateStatus(List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty() || status == null) {
            return false;
        }
        ApPayable apPayable = new ApPayable();
        apPayable.setStatus(status.toString());
        LambdaQueryWrapper<ApPayable> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ApPayable::getId, ids);
        return this.update(apPayable, queryWrapper);
    }
}