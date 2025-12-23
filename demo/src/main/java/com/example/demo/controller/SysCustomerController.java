package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.model.entity.SysCustomer;
import com.example.demo.service.SysCustomerService;
import com.example.demo.model.vo.ResultVO;
import com.example.demo.util.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 客户管理控制器（仅修改分页查询逻辑，解决字段名问题）
 */
@Slf4j
@RestController
@RequestMapping("/accounting/customer")
public class SysCustomerController {

    @Resource
    private SysCustomerService customerService;

    /**
     * 新增客户（未修改）
     */
    @PostMapping("/add")
    public ResultVO addCustomer(@RequestBody SysCustomer customer) {
        log.info("新增客户参数：{}", customer);
        try {
            if (customer.getCustomerName() == null || customer.getCustomerName().trim().isEmpty()) {
                return ResultUtil.systemError("客户名称不能为空");
            }
            if (customer.getStatus() == null || customer.getStatus().trim().isEmpty()) {
                return ResultUtil.systemError("客户状态不能为空");
            }
            // 补充时间字段（若需要）
            Date now = new Date();
            customer.setCreateTime(now);
            customer.setUpdateTime(now);
            boolean success = customerService.save(customer);
            return success ?
                    ResultUtil.success("新增成功", customer.getCustomerId()) :
                    ResultUtil.systemError("新增失败");
        } catch (Exception e) {
            log.error("新增客户异常", e);
            return ResultUtil.systemError("新增失败：" + e.getMessage().split(":")[0]);
        }
    }

    /**
     * 分页查询客户列表（核心修改：强制指定查询字段为 customerId 等驼峰字段）
     */
    @GetMapping("/list")
    public ResultVO getCustomerList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String status
    ) {
        log.info("查询客户列表参数：pageNum={}, pageSize={}, customerName={}, phone={}, status={}",
                pageNum, pageSize, customerName, phone, status);
        try {
            Page<SysCustomer> page = new Page<>(pageNum, pageSize);
            QueryWrapper<SysCustomer> wrapper = new QueryWrapper<>();

            // 关键修改：显式指定所有查询字段，强制使用数据库的 customerId（非 customer_id）
            wrapper.select(
                    "customerId",       // 替换可能被转换的 customer_id
                    "phone",
                    "customerName",
                    "email",
                    "address",
                    "registrationDate",
                    "remarks",
                    "status",
                    "createTime",
                    "updateTime"
            );

            // 条件查询（字段名保持与数据库一致）
            if (customerName != null && !customerName.isEmpty()) {
                wrapper.like("customerName", customerName);
            }
            if (phone != null && !phone.isEmpty()) {
                wrapper.like("phone", phone);
            }
            if (status != null && !status.isEmpty()) {
                wrapper.eq("status", status);
            }

            // 排序字段使用数据库实际字段名
            wrapper.orderByDesc("updateTime");

            IPage<SysCustomer> result = customerService.page(page, wrapper);
            return ResultUtil.success("查询成功", result);
        } catch (Exception e) {
            log.error("查询客户列表异常", e); // 打印完整堆栈，便于定位问题
            return ResultUtil.systemError("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询客户详情（未修改）
     */
    @GetMapping("/detail/{customerId}")
    public ResultVO getCustomerDetail(@PathVariable Long customerId) {
        log.info("查询客户详情：customerId={}", customerId);
        try {
            SysCustomer customer = customerService.getById(customerId);
            if (customer == null) {
                return ResultUtil.systemError("客户不存在");
            }
            return ResultUtil.success("查询成功", customer);
        } catch (Exception e) {
            log.error("查询客户详情异常", e);
            return ResultUtil.systemError("查询失败：" + e.getMessage().split(":")[0]);
        }
    }

    /**
     * 修改客户（未修改）
     */
    @PutMapping("/update")
    public ResultVO updateCustomer(@RequestBody SysCustomer customer) {
        log.info("修改客户参数：{}", customer);
        try {
            if (customer.getCustomerId() == null) {
                return ResultUtil.systemError("客户ID不能为空");
            }
            if (customer.getCustomerName() == null || customer.getCustomerName().trim().isEmpty()) {
                return ResultUtil.systemError("客户名称不能为空");
            }
            if (customer.getStatus() == null || customer.getStatus().trim().isEmpty()) {
                return ResultUtil.systemError("客户状态不能为空");
            }
            customer.setUpdateTime(new Date()); // 更新时间戳
            boolean success = customerService.updateById(customer);
            return success ?
                    ResultUtil.success("修改成功", null) :
                    ResultUtil.systemError("修改失败");
        } catch (Exception e) {
            log.error("修改客户异常", e);
            return ResultUtil.systemError("修改失败：" + e.getMessage().split(":")[0]);
        }
    }

    /**
     * 删除客户（未修改）
     */
    @DeleteMapping("/delete/{customerId}")
    public ResultVO deleteCustomer(@PathVariable Long customerId) {
        log.info("删除客户：customerId={}", customerId);
        try {
            boolean success = customerService.removeById(customerId);
            return success ?
                    ResultUtil.success("删除成功", null) :
                    ResultUtil.systemError("删除失败");
        } catch (Exception e) {
            log.error("删除客户异常", e);
            return ResultUtil.systemError("删除失败：" + e.getMessage().split(":")[0]);
        }
    }

    /**
     * 切换客户状态（未修改）
     */
    @PutMapping("/changeStatus")
    public ResultVO changeStatus(
            @RequestParam Long customerId,
            @RequestParam String status
    ) {
        log.info("切换客户状态：customerId={}, status={}", customerId, status);
        try {
            SysCustomer customer = new SysCustomer();
            customer.setCustomerId(customerId);
            customer.setStatus(status);
            customer.setUpdateTime(new Date());
            boolean success = customerService.updateById(customer);
            return success ?
                    ResultUtil.success("状态切换成功", null) :
                    ResultUtil.systemError("状态切换失败");
        } catch (Exception e) {
            log.error("切换客户状态异常", e);
            return ResultUtil.systemError("状态切换失败：" + e.getMessage().split(":")[0]);
        }
    }
}