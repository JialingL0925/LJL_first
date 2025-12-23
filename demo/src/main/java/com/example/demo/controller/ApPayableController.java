package com.example.demo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.model.entity.ApPayable;
import com.example.demo.model.vo.ResultVO;
import com.example.demo.service.ApPayableService;
import com.example.demo.util.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 应付单控制器
 * 接口前缀: /accounting/apPayable
 */
@Slf4j
@RestController
@RequestMapping("/accounting/apPayable")
public class ApPayableController {

    @Resource
    private ApPayableService apPayableService;

    /**
     * 分页查询应付单列表
     */
    @GetMapping("/list")
    public ResultVO getApPayableList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String payableNo,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String payableDateStart,
            @RequestParam(required = false) String payableDateEnd) {
        log.info("查询应付单列表参数: pageNum={}, pageSize={}, payableNo={}, orderId={}, supplierId={}, status={}, payableDateStart={}, payableDateEnd={}",
                pageNum, pageSize, payableNo, orderId, supplierId, status, payableDateStart, payableDateEnd);
        try {
            Page<ApPayable> page = new Page<>(pageNum, pageSize);
            IPage<ApPayable> pageResult = apPayableService.pageList(page, payableNo, orderId, supplierId, status, payableDateStart, payableDateEnd);
            log.info("查询应付单列表成功, 总数: {}", pageResult.getTotal());
            return ResultUtil.success("查询成功", pageResult);
        } catch (Exception e) {
            log.error("查询应付单列表异常", e);
            return ResultUtil.systemError("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询应付单详情
     */
    @GetMapping("/detail/{id}")
    public ResultVO getApPayableDetail(@PathVariable Long id) {
        log.info("查询应付单详情, ID: {}", id);
        try {
            ApPayable apPayable = apPayableService.getById(id);
            if (apPayable != null) {
                return ResultUtil.success("查询成功", apPayable);
            } else {
                log.warn("应付单不存在, ID: {}", id);
                return ResultUtil.systemError("查询失败: 应付单不存在");
            }
        } catch (Exception e) {
            log.error("查询应付单详情异常, ID: {}", id, e);
            return ResultUtil.systemError("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据订单ID查询应付单
     */
    @GetMapping("/byOrderId/{orderId}")
    public ResultVO getApPayableByOrderId(@PathVariable Long orderId) {
        log.info("根据订单ID查询应付单, 订单ID: {}", orderId);
        try {
            List<ApPayable> apPayables = apPayableService.getByOrderId(orderId);
            return ResultUtil.success("查询成功", apPayables);
        } catch (Exception e) {
            log.error("根据订单ID查询应付单异常, 订单ID: {}", orderId, e);
            return ResultUtil.systemError("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据供应商ID查询应付单
     */
    @GetMapping("/bySupplierId/{supplierId}")
    public ResultVO getApPayableBySupplierId(@PathVariable Long supplierId) {
        log.info("根据供应商ID查询应付单, 供应商ID: {}", supplierId);
        try {
            List<ApPayable> apPayables = apPayableService.getBySupplierId(supplierId);
            return ResultUtil.success("查询成功", apPayables);
        } catch (Exception e) {
            log.error("根据供应商ID查询应付单异常, 供应商ID: {}", supplierId, e);
            return ResultUtil.systemError("查询失败: " + e.getMessage());
        }
    }

    /**
     * 修改应付单信息
     */
    @PutMapping("/update")
    public ResultVO updateApPayable(@RequestBody ApPayable apPayable) {
        log.info("修改应付单请求参数: {}", apPayable);
        try {
            if (apPayable.getId() == null) {
                return ResultUtil.systemError("修改失败: 应付单ID不能为空");
            }
            boolean update = apPayableService.updateById(apPayable);
            if (update) {
                log.info("修改应付单成功, ID: {}", apPayable.getId());
                return ResultUtil.success("修改应付单成功", null);
            } else {
                log.warn("修改应付单失败, ID: {} (可能不存在或未变更)", apPayable.getId());
                return ResultUtil.systemError("修改应付单失败: 未找到该应付单或信息未变更");
            }
        } catch (Exception e) {
            log.error("修改应付单异常", e);
            return ResultUtil.systemError("修改失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新应付单状态
     */
    @PutMapping("/batchChangeStatus")
    public ResultVO batchChangeStatus(
            @RequestParam List<Long> ids,
            @RequestParam Integer status) {
        log.info("批量切换应付单状态请求: ID列表={}, 目标状态={}", ids, status);
        try {
            if (ids == null || ids.isEmpty()) {
                return ResultUtil.systemError("切换失败: 应付单ID列表不能为空");
            }
            if (status == null) {
                return ResultUtil.systemError("切换失败: 状态值不能为空");
            }

            boolean update = apPayableService.batchUpdateStatus(ids, status);

            if (update) {
                String msg = "批量切换应付单状态成功";
                return ResultUtil.success(msg, null);
            } else {
                return ResultUtil.systemError("批量切换失败: 状态未变更");
            }
        } catch (Exception e) {
            log.error("批量切换应付单状态异常", e);
            return ResultUtil.systemError("批量切换失败: " + e.getMessage());
        }
    }

    /**
     * 切换应付单状态
     */
    @PutMapping("/changeStatus")
    public ResultVO changeStatus(
            @RequestParam Long id,
            @RequestParam Integer status) {
        log.info("切换应付单状态请求: ID={}, 目标状态={}", id, status);
        try {
            if (id == null) {
                return ResultUtil.systemError("切换失败: 应付单ID不能为空");
            }
            if (status == null) {
                return ResultUtil.systemError("切换失败: 状态值不能为空");
            }

            ApPayable exist = apPayableService.getById(id);
            if (exist == null) {
                return ResultUtil.systemError("切换失败: 应付单不存在");
            }

            ApPayable apPayable = new ApPayable();
            apPayable.setId(id);
            apPayable.setStatus(status.toString());
            boolean update = apPayableService.updateById(apPayable);

            if (update) {
                String msg = "切换应付单状态成功";
                return ResultUtil.success(msg, null);
            } else {
                return ResultUtil.systemError("切换失败: 状态未变更 (可能已为目标状态)");
            }
        } catch (Exception e) {
            log.error("切换应付单状态异常, ID: {}", id, e);
            return ResultUtil.systemError("切换失败: " + e.getMessage());
        }
    }

    /**
     * 统计未付款订单数量（状态为"待确认"或"已确认"）
     */
    @GetMapping("/countUnpaid")
    public ResultVO countUnpaidPayables() {
        log.info("统计未付款订单数量");
        try {
            Long count = apPayableService.countUnpaidPayables();
            log.info("未付款订单数量: {}", count);
            return ResultUtil.success("查询成功", count);
        } catch (Exception e) {
            log.error("统计未付款订单数量异常", e);
            return ResultUtil.systemError("查询失败: " + e.getMessage());
        }
    }
}