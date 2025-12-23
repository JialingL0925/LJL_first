package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.Map;
import com.example.demo.mapper.*;
import com.example.demo.model.dto.PurchaseOrderDTO;
import com.example.demo.model.entity.*;
import com.example.demo.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Service
public class PurchaseOrderServiceImpl extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder> implements PurchaseOrderService {

    @Autowired
    private PurchaseOrderMapper purchaseOrderMapper;
    @Autowired
    private PurchaseOrderLineMapper purchaseOrderLineMapper;
    @Autowired
    private ApPayableMapper apPayableMapper;
    @Autowired
    private BizAccountMappingMapper bizAccountMappingMapper;
    @Autowired
    private AccountingSubjectMapper accountingSubjectMapper;
    @Autowired
    private JournalEntryMapper journalEntryMapper;

    // 生成订单编号
    private String generateOrderNo() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.format("%03d", new Random().nextInt(999));
        return "CG" + date + random;
    }

    // 生成应付单编号
    private String generatePayableNo() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.format("%03d", new Random().nextInt(999));
        return "AP" + date + random;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveOrder(PurchaseOrderDTO dto) {
        // 1. 处理订单主表（字段名严格匹配数据库）
        PurchaseOrder order = dto.getOrder();
        order.setOrder_no(generateOrderNo()); // 非驼峰：order_no
        order.setStatus("待审核");
        order.setCreate_time(new Date()); // create_time
        order.setCreate_user_id(1L); // create_user_id
        purchaseOrderMapper.insert(order);

        // 2. 处理订单项
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<PurchaseOrderLine> lines = dto.getLines();
        for (PurchaseOrderLine line : lines) {
            line.setOrder_id(order.getId()); // order_id
            // 计算订单项金额、税额
                BigDecimal lineAmount = line.getQuantity().multiply(line.getPrice());
                BigDecimal lineTax = lineAmount.multiply(line.getTax_rate()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP); // tax_rate
            line.setAmount(lineAmount);
            line.setTax_amount(lineTax); // tax_amount
            line.setCreate_time(new Date()); // create_time
            purchaseOrderLineMapper.insert(line);
            // 汇总订单总金额
            totalAmount = totalAmount.add(lineAmount).add(lineTax);
        }

        // 3. 更新订单总金额
        order.setTotal_amount(totalAmount); // total_amount
        purchaseOrderMapper.updateById(order);

        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateOrder(PurchaseOrderDTO dto) {
        // 1. 处理订单主表（字段名严格匹配数据库）
        PurchaseOrder order = dto.getOrder();
        if (order.getId() == null) {
            return false;
        }
        
        // 检查订单状态，已审核的订单不允许编辑
        PurchaseOrder existingOrder = purchaseOrderMapper.selectById(order.getId());
        if (existingOrder == null) {
            throw new RuntimeException("订单不存在");
        }
        if ("已审核".equals(existingOrder.getStatus())) {
            throw new RuntimeException("已审核的订单不允许编辑");
        }
        
        // 更新订单主表（不更新创建时间和创建人）
        order.setUpdate_time(new Date()); // update_time
        purchaseOrderMapper.updateById(order);

        // 2. 删除原有订单项
        LambdaQueryWrapper<PurchaseOrderLine> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PurchaseOrderLine::getOrder_id, order.getId());
        purchaseOrderLineMapper.delete(queryWrapper);

        // 3. 处理新的订单项
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<PurchaseOrderLine> lines = dto.getLines();
        if (lines != null && !lines.isEmpty()) {
            for (PurchaseOrderLine line : lines) {
                line.setOrder_id(order.getId()); // order_id
                // 计算订单项金额、税额
                BigDecimal lineAmount = line.getQuantity().multiply(line.getPrice());
                BigDecimal lineTax = lineAmount.multiply(line.getTax_rate()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP); // tax_rate
                line.setAmount(lineAmount);
                line.setTax_amount(lineTax); // tax_amount
                line.setCreate_time(new Date()); // create_time
                line.setUpdate_time(new Date()); // update_time
                purchaseOrderLineMapper.insert(line);
                // 汇总订单总金额
                totalAmount = totalAmount.add(lineAmount).add(lineTax);
            }
        }

        // 4. 更新订单总金额
        order.setTotal_amount(totalAmount); // total_amount
        purchaseOrderMapper.updateById(order);

        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean auditOrder(Long id, Long userId) {
        try {
            System.out.println("开始审核订单，订单ID: " + id);
            // 1. 查询订单
            PurchaseOrder order = this.getById(id);
            if (order == null) {
                System.out.println("订单不存在，订单ID: " + id);
                return false;
            }
            System.out.println("订单信息: " + order);
            System.out.println("订单状态: " + order.getStatus());
            if (!"待审核".equals(order.getStatus())) {
                System.out.println("订单状态非待审核，无法审核");
                return false;
            }

            // 2. 更新订单状态
            order.setStatus("已审核");
            purchaseOrderMapper.updateById(order);
            System.out.println("订单状态已更新为已审核");

            // 3. 生成应付单
            ApPayable payable = new ApPayable();
            payable.setPayable_no(generatePayableNo());
            payable.setOrder_id(order.getId()); // order_id
            payable.setSupplier_id(order.getSupplier_id()); // supplier_id
            payable.setSupplier_name(order.getSupplier_name()); // supplier_name
            payable.setTotal_amount(order.getTotal_amount()); // total_amount
            payable.setStatus("待确认");
            payable.setCreate_user_id(userId); // create_user_id
            payable.setCreate_time(new Date()); // create_time
            apPayableMapper.insert(payable);
            System.out.println("应付单已生成: " + payable);

            // 4. 查询科目映射规则（参数名匹配数据库字段）
            System.out.println("查询科目映射规则，biz_type: PURCHASE, biz_sub_type: " + order.getBiz_sub_type());
            BizAccountMapping mapping = bizAccountMappingMapper.selectByBizTypeAndSubType("PURCHASE", order.getBiz_sub_type()); // biz_sub_type
            if (mapping == null) {
                System.out.println("未配置【" + order.getBiz_sub_type() + "】的科目映射规则，尝试查询默认的服务采购映射");
                // 如果没有找到映射规则，尝试使用默认的服务采购映射
                mapping = bizAccountMappingMapper.selectByBizTypeAndSubType("PURCHASE", "服务采购");
                if (mapping == null) {
                    throw new RuntimeException("未配置【" + order.getBiz_sub_type() + "】的科目映射规则");
                }
            }
            System.out.println("科目映射规则: " + mapping);

            // 5. 汇总订单项金额（不含税+税额）
            List<PurchaseOrderLine> lines = purchaseOrderLineMapper.selectList(
                    new LambdaQueryWrapper<PurchaseOrderLine>().eq(PurchaseOrderLine::getOrder_id, id) // order_id
            );
            System.out.println("订单项数量: " + lines.size());
            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal totalTax = BigDecimal.ZERO;
            for (PurchaseOrderLine line : lines) {
                totalAmount = totalAmount.add(line.getAmount());
                totalTax = totalTax.add(line.getTax_amount()); // tax_amount
                System.out.println("订单项: " + line + ", 金额: " + line.getAmount() + ", 税额: " + line.getTax_amount());
            }
            System.out.println("总金额: " + totalAmount + ", 总税额: " + totalTax);

            // 6. 查询会计科目
            System.out.println("查询借方科目: " + mapping.getDebit_code());
            AccountingSubject debitSubject = accountingSubjectMapper.selectByCode(mapping.getDebit_code());
            if (debitSubject == null) {
                throw new RuntimeException("未找到借方科目：" + mapping.getDebit_code());
            }
            System.out.println("借方科目: " + debitSubject);
            
            System.out.println("查询贷方科目: " + mapping.getCredit_code());
            AccountingSubject creditSubject = accountingSubjectMapper.selectByCode(mapping.getCredit_code());
            if (creditSubject == null) {
                throw new RuntimeException("未找到贷方科目：" + mapping.getCredit_code());
            }
            System.out.println("贷方科目: " + creditSubject);
            
            System.out.println("查询进项税科目: 22210101");
            AccountingSubject taxSubject = accountingSubjectMapper.selectByCode("22210101");
            if (taxSubject == null) {
                throw new RuntimeException("未找到进项税科目：22210101");
            }
            System.out.println("进项税科目: " + taxSubject);

            // 7. 生成主分录（不含税金额）
            JournalEntry mainEntry = new JournalEntry();
            mainEntry.setSource_type("PURCHASE");
            mainEntry.setSource_id(order.getId());
            mainEntry.setDebit_code(mapping.getDebit_code());
            mainEntry.setDebit_name(debitSubject.getSubjectName());
            mainEntry.setCredit_code(mapping.getCredit_code());
            mainEntry.setCredit_name(creditSubject.getSubjectName());
            mainEntry.setAmount(totalAmount);
            mainEntry.setStatus("待过账"); // 自动生成的分录初始状态为待过账
            mainEntry.setCreate_user_id(userId != null ? userId : 0L); // 确保create_user_id不为null
            mainEntry.setCreate_time(new Date());
            journalEntryMapper.insert(mainEntry);
            System.out.println("主分录已生成: " + mainEntry);
            
            // 8. 生成进项税分录
            JournalEntry taxEntry = new JournalEntry();
            taxEntry.setSource_type("PURCHASE"); // source_type
            taxEntry.setSource_id(order.getId()); // source_id
            taxEntry.setDebit_code("22210101"); // debit_code
            taxEntry.setDebit_name(taxSubject.getSubjectName()); // subject_name
            taxEntry.setCredit_code(mapping.getCredit_code()); // 使用映射中的贷方科目代码
            taxEntry.setCredit_name(creditSubject.getSubjectName()); // subject_name
            taxEntry.setAmount(totalTax);
            taxEntry.setTax_amount(totalTax); // tax_amount
            taxEntry.setStatus("待过账"); // 自动生成的分录初始状态为待过账
            taxEntry.setCreate_user_id(userId != null ? userId : 0L); // 确保create_user_id不为null
            taxEntry.setCreate_time(new Date()); // create_time
            journalEntryMapper.insert(taxEntry);
            System.out.println("进项税分录已生成: " + taxEntry);

            return true;
        } catch (Exception e) {
            // 记录异常日志
            e.printStackTrace();
            // 重新抛出异常，确保事务回滚
            throw new RuntimeException("审核订单失败：" + e.getMessage(), e);
        }
    }

    @Override
    public IPage<PurchaseOrder> pageList(Page<PurchaseOrder> page, String orderNo, Long supplierId, String status, String startTime, String endTime, Long projectId) {
        LambdaQueryWrapper<PurchaseOrder> queryWrapper = new LambdaQueryWrapper<>();
        if (orderNo != null && !orderNo.isEmpty()) {
            queryWrapper.like(PurchaseOrder::getOrder_no, orderNo);
        }
        if (supplierId != null) {
            queryWrapper.eq(PurchaseOrder::getSupplier_id, supplierId);
        }
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(PurchaseOrder::getStatus, status);
        }
        if (projectId != null) {
            queryWrapper.eq(PurchaseOrder::getProject_id, projectId);
        }
        // 按创建时间倒序排序，新订单优先显示
        queryWrapper.orderByDesc(PurchaseOrder::getCreate_time);
        return this.page(page, queryWrapper);
    }

    @Override
    public Map<String, Object> getOrderDetail(Long id) {
        // 这里简单实现，实际应该查询订单详情和订单项
        PurchaseOrder order = this.getById(id);
        if (order == null) {
            return null;
        }
        // 查询订单项
        List<PurchaseOrderLine> lines = purchaseOrderLineMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrderLine>().eq(PurchaseOrderLine::getOrder_id, id)
        );
        // 查询生成的应付单
        List<ApPayable> payables = apPayableMapper.selectList(
                new LambdaQueryWrapper<ApPayable>().eq(ApPayable::getOrder_id, id)
        );
        // 查询生成的分录
        List<JournalEntry> entries = journalEntryMapper.selectList(
                new LambdaQueryWrapper<JournalEntry>().eq(JournalEntry::getSource_id, id).eq(JournalEntry::getSource_type, "PURCHASE")
        );
        // 构造返回结果
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("order", order);
        result.put("lines", lines);
        result.put("payables", payables);
        result.put("entries", entries);
        return result;
    }

    @Override
    public PurchaseOrder getByOrderNo(String orderNo) {
        LambdaQueryWrapper<PurchaseOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PurchaseOrder::getOrder_no, orderNo);
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean batchUpdateStatus(List<Long> ids, String status) {
        if (ids == null || ids.isEmpty() || status == null || status.isEmpty()) {
            return false;
        }
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setStatus(status);
        LambdaQueryWrapper<PurchaseOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(PurchaseOrder::getId, ids);
        return this.update(purchaseOrder, queryWrapper);
    }
}