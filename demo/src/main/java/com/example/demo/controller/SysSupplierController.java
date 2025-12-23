package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.model.entity.SysSupplier;
import com.example.demo.service.SysSupplierService;
import com.example.demo.model.vo.ResultVO;
import com.example.demo.util.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 供应商管理控制器
 * 接口前缀：/accounting/supplier
 */
@Slf4j
@RestController
@RequestMapping("/accounting/supplier")
public class SysSupplierController {

    @Resource
    private SysSupplierService supplierService;

    /**
     * 1. 新增供应商
     */
    @PostMapping("/add")
    public ResultVO addSupplier(@RequestBody SysSupplier supplier) {
        log.info("新增供应商请求参数：{}", supplier);
        try {
            boolean save = supplierService.save(supplier);
            if (save) {
                log.info("新增供应商成功，ID：{}", supplier.getId());
                return ResultUtil.success("新增供应商成功", supplier.getId());
            } else {
                log.warn("新增供应商失败，参数：{}", supplier);
                return ResultUtil.systemError("新增供应商失败");
            }
        } catch (Exception e) {
            log.error("新增供应商异常", e);
            return ResultUtil.systemError("新增供应商失败：" + e.getMessage());
        }
    }

    /**
     * 2. 分页查询供应商列表
     */
    @GetMapping("/list")
    public ResultVO getSupplierList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) String contactPerson,
            @RequestParam(required = false) String status
    ) {
        log.info("查询供应商列表参数：pageNum={}, pageSize={}, supplierName={}, contactPerson={}, status={}",
                pageNum, pageSize, supplierName, contactPerson, status);
        try {
            Page<SysSupplier> page = new Page<>(pageNum, pageSize);
            QueryWrapper<SysSupplier> wrapper = new QueryWrapper<>();
            if (supplierName != null && !supplierName.isEmpty()) {
                wrapper.like("supplier_name", supplierName);
            }
            if (contactPerson != null && !contactPerson.isEmpty()) {
                wrapper.like("contact_person", contactPerson);
            }
            if (status != null && !status.isEmpty()) {
                try {
                    Integer statusInt = Integer.parseInt(status);
                    wrapper.eq("status", statusInt);
                } catch (NumberFormatException e) {
                    log.warn("状态格式错误，传入值：{}", status);
                    return ResultUtil.systemError("查询失败：状态格式错误（需为数字）");
                }
            }
            wrapper.orderByDesc("update_time");
            IPage<SysSupplier> pageResult = supplierService.page(page, wrapper);
            log.info("查询供应商列表成功，总数：{}", pageResult.getTotal());
            return ResultUtil.success("查询成功", pageResult);
        } catch (Exception e) {
            log.error("查询供应商列表异常", e);
            return ResultUtil.systemError("查询失败：" + e.getMessage());
        }
    }

    /**
     * 3. 根据ID查询供应商详情
     */
    @GetMapping("/detail/{id}")
    public ResultVO getSupplierDetail(@PathVariable Long id) {
        log.info("查询供应商详情，ID：{}", id);
        try {
            SysSupplier supplier = supplierService.getById(id);
            if (supplier != null) {
                return ResultUtil.success("查询成功", supplier);
            } else {
                log.warn("供应商不存在，ID：{}", id);
                return ResultUtil.systemError("查询失败：供应商不存在");
            }
        } catch (Exception e) {
            log.error("查询供应商详情异常，ID：{}", id, e);
            return ResultUtil.systemError("查询失败：" + e.getMessage());
        }
    }

    /**
     * 4. 修改供应商信息
     */
    @PutMapping("/update")
    public ResultVO updateSupplier(@RequestBody SysSupplier supplier) {
        log.info("修改供应商请求参数：{}", supplier);
        try {
            if (supplier.getId() == null) {
                return ResultUtil.systemError("修改失败：供应商ID不能为空");
            }
            boolean update = supplierService.updateById(supplier);
            if (update) {
                log.info("修改供应商成功，ID：{}", supplier.getId());
                return ResultUtil.success("修改供应商成功", null);
            } else {
                log.warn("修改供应商失败，ID：{}（可能不存在或未变更）", supplier.getId());
                return ResultUtil.systemError("修改供应商失败：未找到该供应商或信息未变更");
            }
        } catch (Exception e) {
            log.error("修改供应商异常", e);
            return ResultUtil.systemError("修改失败：" + e.getMessage());
        }
    }

    /**
     * 5. 删除供应商
     */
    @DeleteMapping("/delete/{id}")
    public ResultVO deleteSupplier(@PathVariable Long id) {
        log.info("删除供应商请求，ID：{}", id);
        try {
            boolean remove = supplierService.removeById(id);
            if (remove) {
                log.info("删除供应商成功，ID：{}", id);
                return ResultUtil.success("删除供应商成功", null);
            } else {
                log.warn("删除供应商失败，ID：{}（可能不存在）", id);
                return ResultUtil.systemError("删除供应商失败：未找到该供应商");
            }
        } catch (Exception e) {
            log.error("删除供应商异常，ID：{}", id, e);
            return ResultUtil.systemError("删除失败：" + e.getMessage());
        }
    }

    /**
     * 6. 切换供应商状态（仅修改状态值校验为1和0）
     */
    @PutMapping("/changeStatus")
    public ResultVO changeStatus(
            @RequestParam Long id,  // 强制接收id（与前端params一致）
            @RequestParam Integer status  // 强制接收status（与前端params一致）
    ) {
        log.info("切换供应商状态请求：ID={}, 目标状态={}", id, status);
        try {
            // 1. 校验参数合法性（改为1-启用，0-停用）
            if (id == null) {
                return ResultUtil.systemError("切换失败：供应商ID不能为空");
            }
            if (status == null || (status != 1 && status != 0)) {  // 仅修改此处，允许1和0
                return ResultUtil.systemError("切换失败：状态值必须为1（启用）或0（停用）");
            }

            // 2. 检查供应商是否存在
            SysSupplier exist = supplierService.getById(id);
            if (exist == null) {
                return ResultUtil.systemError("切换失败：供应商不存在");
            }

            // 3. 执行状态更新
            SysSupplier supplier = new SysSupplier();
            supplier.setId(id);
            supplier.setStatus(status);
            boolean update = supplierService.updateById(supplier);

            if (update) {
                String msg = status == 1 ? "启用供应商成功" : "停用供应商成功";  // 状态文案对应修改
                return ResultUtil.success(msg, null);
            } else {
                return ResultUtil.systemError("切换失败：状态未变更（可能已为目标状态）");
            }
        } catch (Exception e) {
            log.error("切换供应商状态异常，ID：{}", id, e);
            return ResultUtil.systemError("切换失败：" + e.getMessage());
        }
    }
}