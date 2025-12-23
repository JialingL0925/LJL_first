package com.example.demo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.model.dto.PurchaseOrderDTO;
import com.example.demo.model.entity.PurchaseOrder;
import com.example.demo.model.vo.ResultVO;
import com.example.demo.service.PurchaseOrderService;
import com.example.demo.util.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 采购订单控制器
 * 接口前缀: /accounting/purchaseOrder
 */
@Slf4j
@RestController
@RequestMapping("/accounting/purchaseOrder")
public class PurchaseOrderController {

    @Resource
    private PurchaseOrderService purchaseOrderService;

    /**
     * 新增采购订单
     */
    @PostMapping("/add")
    public ResultVO addPurchaseOrder(@RequestBody PurchaseOrderDTO purchaseOrderDTO) {
        log.info("新增采购订单请求参数: {}", purchaseOrderDTO);
        try {
            boolean save = purchaseOrderService.saveOrder(purchaseOrderDTO);
            if (save) {
                log.info("新增采购订单成功, ID: {}", purchaseOrderDTO.getOrder().getId());
                return ResultUtil.success("新增采购订单成功", purchaseOrderDTO.getOrder().getId());
            } else {
                log.warn("新增采购订单失败, 参数: {}", purchaseOrderDTO);
                return ResultUtil.systemError("新增采购订单失败");
            }
        } catch (Exception e) {
            log.error("新增采购订单异常", e);
            return ResultUtil.systemError("新增采购订单失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询采购订单列表
     */
    @GetMapping("/list")
    public ResultVO getPurchaseOrderList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) Long projectId) {
        log.info("查询采购订单列表参数: pageNum={}, pageSize={}, orderNo={}, supplierId={}, status={}, startTime={}, endTime={}, projectId={}",
                pageNum, pageSize, orderNo, supplierId, status, startTime, endTime, projectId);
        try {
            Page<PurchaseOrder> page = new Page<>(pageNum, pageSize);
            IPage<PurchaseOrder> pageResult = purchaseOrderService.pageList(page, orderNo, supplierId, status, startTime, endTime, projectId);
            log.info("查询采购订单列表成功, 总数: {}", pageResult.getTotal());
            return ResultUtil.success("查询成功", pageResult);
        } catch (Exception e) {
            log.error("查询采购订单列表异常", e);
            return ResultUtil.systemError("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询采购订单详情（包含订单明细）
     */
    @GetMapping("/detail/{id}")
    public ResultVO getPurchaseOrderDetail(@PathVariable Long id) {
        log.info("查询采购订单详情, ID: {}", id);
        try {
            Map<String, Object> detail = purchaseOrderService.getOrderDetail(id);
            if (detail != null) {
                return ResultUtil.success("查询成功", detail);
            } else {
                log.warn("采购订单不存在, ID: {}", id);
                return ResultUtil.systemError("查询失败: 采购订单不存在");
            }
        } catch (Exception e) {
            log.error("查询采购订单详情异常, ID: {}", id, e);
            return ResultUtil.systemError("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据订单号查询采购订单
     */
    @GetMapping("/byOrderNo/{orderNo}")
    public ResultVO getPurchaseOrderByOrderNo(@PathVariable String orderNo) {
        log.info("根据订单号查询采购订单, 订单号: {}", orderNo);
        try {
            PurchaseOrder purchaseOrder = purchaseOrderService.getByOrderNo(orderNo);
            if (purchaseOrder != null) {
                return ResultUtil.success("查询成功", purchaseOrder);
            } else {
                log.warn("采购订单不存在, 订单号: {}", orderNo);
                return ResultUtil.systemError("查询失败: 采购订单不存在");
            }
        } catch (Exception e) {
            log.error("根据订单号查询采购订单异常, 订单号: {}", orderNo, e);
            return ResultUtil.systemError("查询失败: " + e.getMessage());
        }
    }

    /**
     * 修改采购订单信息
     */
    @PutMapping("/update")
    public ResultVO updatePurchaseOrder(@RequestBody PurchaseOrderDTO purchaseOrderDTO) {
        log.info("修改采购订单请求参数: {}", purchaseOrderDTO);
        try {
            PurchaseOrder purchaseOrder = purchaseOrderDTO.getOrder();
            if (purchaseOrder == null || purchaseOrder.getId() == null) {
                return ResultUtil.systemError("修改失败: 采购订单信息不能为空");
            }
            boolean update = purchaseOrderService.updateOrder(purchaseOrderDTO);
            if (update) {
                log.info("修改采购订单成功, ID: {}", purchaseOrder.getId());
                return ResultUtil.success("修改采购订单成功", null);
            } else {
                log.warn("修改采购订单失败, ID: {} (可能不存在或未变更)", purchaseOrder.getId());
                return ResultUtil.systemError("修改采购订单失败: 未找到该采购订单或信息未变更");
            }
        } catch (Exception e) {
            log.error("修改采购订单异常", e);
            // 如果是业务异常（如已审核不允许编辑），返回具体错误信息
            if (e.getMessage() != null && e.getMessage().contains("已审核")) {
                return ResultUtil.error(e.getMessage());
            }
            return ResultUtil.systemError("修改失败: " + e.getMessage());
        }
    }

    /**
     * 删除采购订单
     */
    @DeleteMapping("/delete/{id}")
    public ResultVO deletePurchaseOrder(@PathVariable Long id) {
        log.info("删除采购订单请求, ID: {}", id);
        try {
            boolean remove = purchaseOrderService.removeById(id);
            if (remove) {
                log.info("删除采购订单成功, ID: {}", id);
                return ResultUtil.success("删除采购订单成功", null);
            } else {
                log.warn("删除采购订单失败, ID: {} (可能不存在)", id);
                return ResultUtil.systemError("删除采购订单失败: 未找到该采购订单");
            }
        } catch (Exception e) {
            log.error("删除采购订单异常, ID: {}", id, e);
            return ResultUtil.systemError("删除失败: " + e.getMessage());
        }
    }

    /**
     * 切换采购订单状态
     */
    @PutMapping("/changeStatus")
    public ResultVO changeStatus(
            @RequestParam Long id,
            @RequestParam String status) {
        log.info("切换采购订单状态请求: ID={}, 目标状态={}", id, status);
        try {
            if (id == null) {
                return ResultUtil.systemError("切换失败: 采购订单ID不能为空");
            }
            if (status == null || status.isEmpty()) {
                return ResultUtil.systemError("切换失败: 状态值不能为空");
            }

            PurchaseOrder exist = purchaseOrderService.getById(id);
            if (exist == null) {
                return ResultUtil.systemError("切换失败: 采购订单不存在");
            }

            boolean success;
            // 如果是审核操作，调用完整的审核流程
            if ("已审核".equals(status)) {
                // 这里简化处理，userId硬编码为1，实际项目中应从登录信息获取
                success = purchaseOrderService.auditOrder(id, 1L);
            } else {
                // 其他状态更新直接修改状态
                PurchaseOrder purchaseOrder = new PurchaseOrder();
                purchaseOrder.setId(id);
                purchaseOrder.setStatus(status);
                success = purchaseOrderService.updateById(purchaseOrder);
            }

            if (success) {
                String msg = "切换采购订单状态成功";
                return ResultUtil.success(msg, null);
            } else {
                return ResultUtil.systemError("切换失败: 状态未变更 (可能已为目标状态)");
            }
        } catch (Exception e) {
            log.error("切换采购订单状态异常, ID: {}", id, e);
            return ResultUtil.systemError("切换失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新采购订单状态
     */
    @PutMapping("/batchChangeStatus")
    public ResultVO batchChangeStatus(
            @RequestParam List<Long> ids,
            @RequestParam String status) {
        log.info("批量切换采购订单状态请求: ID列表={}, 目标状态={}", ids, status);
        try {
            if (ids == null || ids.isEmpty()) {
                return ResultUtil.systemError("切换失败: 采购订单ID列表不能为空");
            }
            if (status == null || status.isEmpty()) {
                return ResultUtil.systemError("切换失败: 状态值不能为空");
            }

            boolean update = purchaseOrderService.batchUpdateStatus(ids, status);

            if (update) {
                String msg = "批量切换采购订单状态成功";
                return ResultUtil.success(msg, null);
            } else {
                return ResultUtil.systemError("批量切换失败: 状态未变更");
            }
        } catch (Exception e) {
            log.error("批量切换采购订单状态异常", e);
            return ResultUtil.systemError("批量切换失败: " + e.getMessage());
        }
    }
}