package com.example.demo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.model.entity.ArReceivable;
import com.example.demo.model.vo.ResultVO;
import com.example.demo.service.ArReceivableService;
import com.example.demo.util.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 应收单控制器
 * 接口前缀: /accounting/arReceivable
 */
@Slf4j
@RestController
@RequestMapping("/accounting/arReceivable")
public class ArReceivableController {

    @Resource
    private ArReceivableService arReceivableService;

    /**
     * 分页查询应收单列表
     */
    @GetMapping("/list")
    public ResultVO getArReceivableList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String receivableNo,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String status) {
        log.info("查询应收单列表参数: pageNum={}, pageSize={}, receivableNo={}, orderId={}, customerId={}, customerName={}, status={}",
                pageNum, pageSize, receivableNo, orderId, customerId, customerName, status);
        try {
            Page<ArReceivable> page = new Page<>(pageNum, pageSize);
            IPage<ArReceivable> pageResult = arReceivableService.pageList(page, receivableNo, orderId, customerId, status, null, null);
            log.info("查询应收单列表成功, 总数: {}", pageResult.getTotal());
            return ResultUtil.success("查询成功", pageResult);
        } catch (Exception e) {
            log.error("查询应收单列表异常", e);
            return ResultUtil.systemError("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询应收单详情
     */
    @GetMapping("/detail/{id}")
    public ResultVO getArReceivableDetail(@PathVariable Long id) {
        log.info("查询应收单详情, ID: {}", id);
        try {
            ArReceivable arReceivable = arReceivableService.getById(id);
            if (arReceivable != null) {
                return ResultUtil.success("查询成功", arReceivable);
            } else {
                log.warn("应收单不存在, ID: {}", id);
                return ResultUtil.systemError("查询失败: 应收单不存在");
            }
        } catch (Exception e) {
            log.error("查询应收单详情异常, ID: {}", id, e);
            return ResultUtil.systemError("查询失败: " + e.getMessage());
        }
    }

    /**
     * 统计未收款订单数量（状态为"待确认"或"已确认"）
     */
    @GetMapping("/countUnpaid")
    public ResultVO countUnpaidReceivables() {
        log.info("统计未收款订单数量");
        try {
            Long count = arReceivableService.countUnpaidReceivables();
            log.info("未收款订单数量: {}", count);
            return ResultUtil.success("查询成功", count);
        } catch (Exception e) {
            log.error("统计未收款订单数量异常", e);
            return ResultUtil.systemError("查询失败: " + e.getMessage());
        }
    }
}