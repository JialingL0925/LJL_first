package com.example.demo.controller;

import com.example.demo.model.entity.PurchaseOrderLine;
import com.example.demo.model.vo.ResultVO;
import com.example.demo.util.ResultUtil;
import com.example.demo.mapper.PurchaseOrderLineMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 采购订单项控制器
 * 接口前缀: /accounting/purchaseOrderLine
 */
@Slf4j
@RestController
@RequestMapping("/accounting/purchaseOrderLine")
public class PurchaseOrderLineController {

    @Resource
    private PurchaseOrderLineMapper purchaseOrderLineMapper;

    /**
     * 根据订单ID查询采购订单项列表
     */
    @GetMapping("/list/{orderId}")
    public ResultVO getPurchaseOrderLines(@PathVariable Long orderId) {
        log.info("根据订单ID查询采购订单项列表参数: orderId={}", orderId);
        try {
            if (orderId == null) {
                return ResultUtil.systemError("查询失败: 订单ID不能为空");
            }
            
            // 查询该订单下的所有订单项
            List<PurchaseOrderLine> lines = purchaseOrderLineMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PurchaseOrderLine>()
                            .eq(PurchaseOrderLine::getOrder_id, orderId)
            );
            
            log.info("查询采购订单项列表成功, 订单ID: {}, 数量: {}", orderId, lines.size());
            return ResultUtil.success("查询成功", lines);
        } catch (Exception e) {
            log.error("查询采购订单项列表异常, orderId: {}", orderId, e);
            return ResultUtil.systemError("查询失败: " + e.getMessage());
        }
    }

    /**
     * 新增采购订单项
     */
    @PostMapping("/add")
    public ResultVO addPurchaseOrderLine(@RequestBody PurchaseOrderLine purchaseOrderLine) {
        log.info("新增采购订单项请求参数: {}", purchaseOrderLine);
        try {
            if (purchaseOrderLine == null) {
                return ResultUtil.systemError("新增失败: 订单项信息不能为空");
            }
            if (purchaseOrderLine.getOrder_id() == null) {
                return ResultUtil.systemError("新增失败: 订单ID不能为空");
            }
            
            // 设置创建时间
            purchaseOrderLine.setCreate_time(new Date());
            
            // 插入数据库
            boolean insert = purchaseOrderLineMapper.insert(purchaseOrderLine) > 0;
            if (insert) {
                log.info("新增采购订单项成功, ID: {}", purchaseOrderLine.getId());
                return ResultUtil.success("新增采购订单项成功", purchaseOrderLine.getId());
            } else {
                log.warn("新增采购订单项失败, 参数: {}", purchaseOrderLine);
                return ResultUtil.systemError("新增采购订单项失败");
            }
        } catch (Exception e) {
            log.error("新增采购订单项异常", e);
            return ResultUtil.systemError("新增采购订单项失败: " + e.getMessage());
        }
    }

    /**
     * 批量新增采购订单项
     */
    @PostMapping("/batchAdd")
    public ResultVO batchAddPurchaseOrderLines(@RequestBody List<PurchaseOrderLine> purchaseOrderLines) {
        log.info("批量新增采购订单项请求参数: {}", purchaseOrderLines);
        try {
            if (purchaseOrderLines == null || purchaseOrderLines.isEmpty()) {
                return ResultUtil.systemError("批量新增失败: 订单项列表不能为空");
            }
            
            // 检查每个订单项的订单ID是否存在
            for (PurchaseOrderLine line : purchaseOrderLines) {
                if (line.getOrder_id() == null) {
                    return ResultUtil.systemError("批量新增失败: 订单项的订单ID不能为空");
                }
                line.setCreate_time(new Date());
            }
            
            // 批量插入数据库（使用循环插入）
            for (PurchaseOrderLine line : purchaseOrderLines) {
                purchaseOrderLineMapper.insert(line);
            }
            
            log.info("批量新增采购订单项成功, 数量: {}", purchaseOrderLines.size());
            return ResultUtil.success("批量新增采购订单项成功", purchaseOrderLines.size());
        } catch (Exception e) {
            log.error("批量新增采购订单项异常", e);
            return ResultUtil.systemError("批量新增采购订单项失败: " + e.getMessage());
        }
    }
}