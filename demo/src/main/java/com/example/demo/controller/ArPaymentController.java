package com.example.demo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.model.entity.ArPayment;
import com.example.demo.model.vo.ResultVO;
import com.example.demo.service.ArPaymentService;
import com.example.demo.util.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 收款单控制器
 * 接口前缀: /accounting/arPayment
 */
@Slf4j
@RestController
@RequestMapping("/accounting/arPayment")
public class ArPaymentController {

    @Resource
    private ArPaymentService arPaymentService;

    /**
     * 分页查询收款单列表
     */
    @GetMapping("/list")
    public ResultVO getArPaymentList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String paymentNo,
            @RequestParam(required = false) Long receivableId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentDateStart,
            @RequestParam(required = false) String paymentDateEnd) {
        log.info("查询收款单列表参数: pageNum={}, pageSize={}, paymentNo={}, receivableId={}, customerId={}, status={}, paymentDateStart={}, paymentDateEnd={}",
                pageNum, pageSize, paymentNo, receivableId, customerId, status, paymentDateStart, paymentDateEnd);
        try {
            Page<ArPayment> page = new Page<>(pageNum, pageSize);
            IPage<ArPayment> pageResult = arPaymentService.pageList(page, paymentNo, receivableId, customerId, status, paymentDateStart, paymentDateEnd);
            log.info("查询收款单列表成功, 总数: {}", pageResult.getTotal());
            return ResultUtil.success("查询成功", pageResult);
        } catch (Exception e) {
            log.error("查询收款单列表异常", e);
            return ResultUtil.systemError("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询收款单详情
     */
    @GetMapping("/detail/{id}")
    public ResultVO getArPaymentDetail(@PathVariable Long id) {
        log.info("查询收款单详情, ID: {}", id);
        try {
            ArPayment arPayment = arPaymentService.getById(id);
            if (arPayment != null) {
                return ResultUtil.success("查询成功", arPayment);
            } else {
                log.warn("收款单不存在, ID: {}", id);
                return ResultUtil.systemError("查询失败: 收款单不存在");
            }
        } catch (Exception e) {
            log.error("查询收款单详情异常, ID: {}", id, e);
            return ResultUtil.systemError("查询失败: " + e.getMessage());
        }
    }

    /**
     * 创建收款单
     */
    @PostMapping("/create")
    public ResultVO createArPayment(@RequestBody ArPayment arPayment) {
        log.info("创建收款单请求参数: {}", arPayment);
        try {
            boolean create = arPaymentService.createPayment(arPayment);
            if (create) {
                log.info("创建收款单成功");
                return ResultUtil.success("创建收款单成功", null);
            } else {
                log.warn("创建收款单失败");
                return ResultUtil.systemError("创建收款单失败");
            }
        } catch (Exception e) {
            log.error("创建收款单异常", e);
            return ResultUtil.systemError("创建失败: " + e.getMessage());
        }
    }

    /**
     * 确认收款单
     */
    @PutMapping("/confirm/{id}")
    public ResultVO confirmArPayment(@PathVariable Long id, @RequestParam Long userId) {
        log.info("确认收款单, ID: {}, 用户ID: {}", id, userId);
        try {
            boolean confirm = arPaymentService.confirmPayment(id, userId);
            if (confirm) {
                log.info("确认收款单成功, ID: {}", id);
                return ResultUtil.success("确认收款单成功", null);
            } else {
                log.warn("确认收款单失败, ID: {}", id);
                return ResultUtil.systemError("确认收款单失败");
            }
        } catch (Exception e) {
            log.error("确认收款单异常, ID: {}", id, e);
            return ResultUtil.systemError("确认失败: " + e.getMessage());
        }
    }

    /**
     * 取消收款单
     */
    @PutMapping("/cancel/{id}")
    public ResultVO cancelArPayment(@PathVariable Long id) {
        log.info("取消收款单, ID: {}", id);
        try {
            boolean cancel = arPaymentService.cancelPayment(id);
            if (cancel) {
                log.info("取消收款单成功, ID: {}", id);
                return ResultUtil.success("取消收款单成功", null);
            } else {
                log.warn("取消收款单失败, ID: {}", id);
                return ResultUtil.systemError("取消收款单失败: 收款单状态可能已变更");
            }
        } catch (Exception e) {
            log.error("取消收款单异常, ID: {}", id, e);
            return ResultUtil.systemError("取消失败: " + e.getMessage());
        }
    }
}