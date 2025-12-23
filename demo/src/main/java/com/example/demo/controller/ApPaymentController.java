package com.example.demo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.model.entity.ApPayment;
import com.example.demo.model.vo.ResultVO;
import com.example.demo.service.ApPaymentService;
import com.example.demo.util.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 付款单控制器
 * 接口前缀: /accounting/apPayment
 */
@Slf4j
@RestController
@RequestMapping("/accounting/apPayment")
public class ApPaymentController {

    @Resource
    private ApPaymentService apPaymentService;

    /**
     * 分页查询付款单列表
     */
    @GetMapping("/list")
    public ResultVO getApPaymentList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String paymentNo,
            @RequestParam(required = false) Long payableId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentDateStart,
            @RequestParam(required = false) String paymentDateEnd) {
        log.info("查询付款单列表参数: pageNum={}, pageSize={}, paymentNo={}, payableId={}, supplierId={}, status={}, paymentDateStart={}, paymentDateEnd={}",
                pageNum, pageSize, paymentNo, payableId, supplierId, status, paymentDateStart, paymentDateEnd);
        try {
            Page<ApPayment> page = new Page<>(pageNum, pageSize);
            IPage<ApPayment> pageResult = apPaymentService.pageList(page, paymentNo, payableId, supplierId, status, paymentDateStart, paymentDateEnd);
            log.info("查询付款单列表成功, 总数: {}", pageResult.getTotal());
            return ResultUtil.success("查询成功", pageResult);
        } catch (Exception e) {
            log.error("查询付款单列表异常", e);
            return ResultUtil.systemError("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询付款单详情
     */
    @GetMapping("/detail/{id}")
    public ResultVO getApPaymentDetail(@PathVariable Long id) {
        log.info("查询付款单详情, ID: {}", id);
        try {
            ApPayment apPayment = apPaymentService.getById(id);
            if (apPayment != null) {
                return ResultUtil.success("查询成功", apPayment);
            } else {
                log.warn("付款单不存在, ID: {}", id);
                return ResultUtil.systemError("查询失败: 付款单不存在");
            }
        } catch (Exception e) {
            log.error("查询付款单详情异常, ID: {}", id, e);
            return ResultUtil.systemError("查询失败: " + e.getMessage());
        }
    }

    /**
     * 创建付款单
     */
    @PostMapping("/create")
    public ResultVO createApPayment(@RequestBody ApPayment apPayment) {
        log.info("创建付款单请求参数: {}", apPayment);
        try {
            boolean create = apPaymentService.createPayment(apPayment);
            if (create) {
                log.info("创建付款单成功");
                return ResultUtil.success("创建付款单成功", null);
            } else {
                log.warn("创建付款单失败");
                return ResultUtil.systemError("创建付款单失败");
            }
        } catch (Exception e) {
            log.error("创建付款单异常", e);
            return ResultUtil.systemError("创建失败: " + e.getMessage());
        }
    }

    /**
     * 确认付款单
     */
    @PutMapping("/confirm/{id}")
    public ResultVO confirmApPayment(@PathVariable Long id, @RequestParam Long userId) {
        log.info("确认付款单, ID: {}, 用户ID: {}", id, userId);
        try {
            boolean confirm = apPaymentService.confirmPayment(id, userId);
            if (confirm) {
                log.info("确认付款单成功, ID: {}", id);
                return ResultUtil.success("确认付款单成功", null);
            } else {
                log.warn("确认付款单失败, ID: {}", id);
                return ResultUtil.systemError("确认付款单失败");
            }
        } catch (Exception e) {
            log.error("确认付款单异常, ID: {}", id, e);
            return ResultUtil.systemError("确认失败: " + e.getMessage());
        }
    }

    /**
     * 取消付款单
     */
    @PutMapping("/cancel/{id}")
    public ResultVO cancelApPayment(@PathVariable Long id) {
        log.info("取消付款单, ID: {}", id);
        try {
            boolean cancel = apPaymentService.cancelPayment(id);
            if (cancel) {
                log.info("取消付款单成功, ID: {}", id);
                return ResultUtil.success("取消付款单成功", null);
            } else {
                log.warn("取消付款单失败, ID: {}", id);
                return ResultUtil.systemError("取消付款单失败: 付款单状态可能已变更");
            }
        } catch (Exception e) {
            log.error("取消付款单异常, ID: {}", id, e);
            return ResultUtil.systemError("取消失败: " + e.getMessage());
        }
    }
}