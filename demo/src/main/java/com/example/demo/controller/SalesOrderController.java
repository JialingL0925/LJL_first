package com.example.demo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.model.dto.SalesOrderDTO;
import com.example.demo.model.entity.SalesOrder;
import com.example.demo.model.vo.ResultVO;
import com.example.demo.service.SalesOrderService;
import com.example.demo.util.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/accounting/salesOrder")
public class SalesOrderController {

    @Autowired
    private SalesOrderService salesOrderService;

    /**
     * 保存销售订单
     */
    @PostMapping("/add")
    public ResultVO addSalesOrder(@RequestBody SalesOrderDTO dto) {
        try {
            boolean result = salesOrderService.saveOrder(dto);
            if (result) {
                return ResultUtil.success("保存销售订单成功");
            } else {
                return ResultUtil.systemError("保存销售订单失败");
            }
        } catch (Exception e) {
            return ResultUtil.systemError(e.getMessage());
        }
    }

    /**
     * 更新销售订单
     */
    @PutMapping("/update")
    public ResultVO updateSalesOrder(@RequestBody SalesOrderDTO dto) {
        try {
            boolean result = salesOrderService.updateOrder(dto);
            if (result) {
                return ResultUtil.success("更新销售订单成功");
            } else {
                return ResultUtil.systemError("更新销售订单失败");
            }
        } catch (Exception e) {
            return ResultUtil.systemError(e.getMessage());
        }
    }

    /**
     * 审核销售订单
     */
    @PutMapping("/audit")
    public ResultVO auditOrder(@RequestParam Long id, @RequestParam Long userId) {
        try {
            boolean result = salesOrderService.auditOrder(id, userId);
            if (result) {
                return ResultUtil.success("审核销售订单成功");
            } else {
                return ResultUtil.systemError("审核销售订单失败");
            }
        } catch (Exception e) {
            return ResultUtil.systemError(e.getMessage());
        }
    }

    /**
     * 分页查询销售订单列表
     */
    @GetMapping("/list")
    public ResultVO getSalesOrderList(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String orderType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) Long projectId) {
        try {
            Page<SalesOrder> page = new Page<>(pageNum, pageSize);
            IPage<SalesOrder> result = salesOrderService.pageList(page, orderNo, customerName, orderType, status, startTime, endTime, projectId);
            return ResultUtil.success("查询销售订单列表成功", result);
        } catch (Exception e) {
            return ResultUtil.systemError(e.getMessage());
        }
    }

    /**
     * 获取销售订单详情
     */
    @GetMapping("/detail/{id}")
    public ResultVO getSalesOrderDetail(@PathVariable Long id) {
        try {
            Map<String, Object> detail = salesOrderService.getOrderDetail(id);
            return ResultUtil.success("查询销售订单详情成功", detail);
        } catch (Exception e) {
            return ResultUtil.systemError(e.getMessage());
        }
    }

    /**
     * 根据订单编号查询销售订单
     */
    @GetMapping("/byOrderNo/{orderNo}")
    public ResultVO getSalesOrderByOrderNo(@PathVariable String orderNo) {
        try {
            SalesOrder salesOrder = salesOrderService.getByOrderNo(orderNo);
            return ResultUtil.success("根据订单编号查询成功", salesOrder);
        } catch (Exception e) {
            return ResultUtil.systemError(e.getMessage());
        }
    }

    /**
     * 批量更新销售订单状态
     */
    @PutMapping("/batchUpdateStatus")
    public ResultVO batchUpdateSalesOrderStatus(@RequestParam List<Long> ids, @RequestParam String status) {
        try {
            boolean result = salesOrderService.batchUpdateStatus(ids, status);
            if (result) {
                return ResultUtil.success("批量更新状态成功");
            } else {
                return ResultUtil.systemError("批量更新状态失败");
            }
        } catch (Exception e) {
            return ResultUtil.systemError(e.getMessage());
        }
    }
}