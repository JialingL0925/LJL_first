package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.mapper.*;
import com.example.demo.model.dto.SalesOrderDTO;
import com.example.demo.model.entity.*;
import com.example.demo.service.*;
import com.example.demo.util.SnowflakeIdUtil;
import com.example.demo.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class SalesOrderServiceImpl extends ServiceImpl<SalesOrderMapper, SalesOrder> implements SalesOrderService {

    @Autowired
    private SalesOrderMapper salesOrderMapper;
    @Autowired
    private SalesOrderLineMapper salesOrderLineMapper;
    @Autowired
    private ArReceivableMapper arReceivableMapper;
    @Autowired
    private JournalEntryMapper journalEntryMapper;
    @Autowired
    private BizAccountMappingMapper bizAccountMappingMapper;
    @Autowired
    private AccountingSubjectMapper accountingSubjectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrder(SalesOrderDTO dto) {
        try {
            // 生成订单编号
            String orderNo = generateOrderNo();
            
            // 保存订单主表
            SalesOrder salesOrder = dto.getOrder();
            salesOrder.setOrder_no(orderNo);
            salesOrder.setTotal_amount(calculateTotalAmount(dto.getLines()));
            salesOrder.setStatus("DRAFT");
            salesOrder.setCreate_time(new Date());
            
            // 确保业务子类型不为空
            if (salesOrder.getBiz_sub_type() == null || salesOrder.getBiz_sub_type().isEmpty()) {
                salesOrder.setBiz_sub_type("服务销售"); // 默认服务销售
                log.info("业务子类型为空，设置默认值: {}", salesOrder.getBiz_sub_type());
            }
            
            salesOrderMapper.insert(salesOrder);
            log.info("销售订单保存成功，订单ID: {}, 业务子类型: {}", salesOrder.getId(), salesOrder.getBiz_sub_type());

            // 保存订单明细
            saveOrderLines(dto.getLines(), salesOrder.getId());
            
            return true;
        } catch (Exception e) {
            log.error("保存销售订单失败: {}", e.getMessage(), e);
            throw new RuntimeException("保存销售订单失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrder(SalesOrderDTO dto) {
        try {
            // 更新订单主表
            SalesOrder salesOrder = dto.getOrder();
            
            // 检查订单状态，已审核的订单不允许编辑
            if (salesOrder.getId() != null) {
                SalesOrder existingOrder = salesOrderMapper.selectById(salesOrder.getId());
                if (existingOrder == null) {
                    throw new RuntimeException("订单不存在");
                }
                if ("APPROVED".equals(existingOrder.getStatus())) {
                    throw new RuntimeException("已审核的订单不允许编辑");
                }
            }
            
            salesOrder.setTotal_amount(calculateTotalAmount(dto.getLines()));
            salesOrder.setUpdate_time(new Date());
            
            // 确保业务子类型不为空
            if (salesOrder.getBiz_sub_type() == null || salesOrder.getBiz_sub_type().isEmpty()) {
                salesOrder.setBiz_sub_type("服务销售"); // 默认服务销售
                log.info("业务子类型为空，设置默认值: {}", salesOrder.getBiz_sub_type());
            }
            
            salesOrderMapper.updateById(salesOrder);
            log.info("销售订单更新成功，订单ID: {}, 业务子类型: {}", salesOrder.getId(), salesOrder.getBiz_sub_type());

            // 删除原有明细
            QueryWrapper<SalesOrderLine> lineWrapper = new QueryWrapper<>();
            lineWrapper.eq("order_id", salesOrder.getId());
            salesOrderLineMapper.delete(lineWrapper);

            // 保存新明细
            saveOrderLines(dto.getLines(), salesOrder.getId());
            
            return true;
        } catch (Exception e) {
            log.error("更新销售订单失败: {}", e.getMessage(), e);
            throw new RuntimeException("更新销售订单失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditOrder(Long id, Long userId) {
        try {
            // 检查订单状态
            SalesOrder salesOrder = salesOrderMapper.selectById(id);
            if (salesOrder == null) {
                throw new RuntimeException("订单不存在");
            }
            if (!"DRAFT".equals(salesOrder.getStatus()) && !"PENDING".equals(salesOrder.getStatus())) {
                throw new RuntimeException("只有草稿或待审核状态的订单可以审核");
            }

            // 1. 更新订单状态为已审核
            salesOrder.setStatus("APPROVED");
            salesOrderMapper.updateById(salesOrder);

            // 2. 生成应收款单
            ArReceivable receivable = new ArReceivable();
            receivable.setReceivable_no(generateReceivableNo());
            receivable.setOrder_id(salesOrder.getId());
            receivable.setCustomer_id(salesOrder.getCustomer_id());
            receivable.setCustomer_name(salesOrder.getCustomer_name());
            receivable.setTotal_amount(salesOrder.getTotal_amount());
            receivable.setStatus("待确认"); // 改为与采购订单应付单一致的状态
            receivable.setCreate_user_id(userId);
            receivable.setCreate_time(new Date());
            arReceivableMapper.insert(receivable);

            // 3. 生成会计分录
            generateAccountingEntries(salesOrder, receivable);
            
            return true;
        } catch (Exception e) {
            log.error("审核销售订单失败: {}", e.getMessage(), e);
            throw new RuntimeException("审核销售订单失败: " + e.getMessage());
        }
    }

    @Override
    public IPage<SalesOrder> pageList(Page<SalesOrder> page, String orderNo, String customerName, String orderType, String status, String startTime, String endTime, Long projectId) {
        QueryWrapper<SalesOrder> wrapper = new QueryWrapper<>();
        
        if (StringUtil.isNotBlank(orderNo)) {
            wrapper.like("order_no", orderNo);
        }
        if (StringUtil.isNotBlank(customerName)) {
            wrapper.like("customer_name", customerName);
        }
        if (StringUtil.isNotBlank(orderType)) {
            wrapper.eq("biz_sub_type", orderType);
        }
        if (StringUtil.isNotBlank(status)) {
            wrapper.eq("status", status);
        }
        if (StringUtil.isNotBlank(startTime)) {
            wrapper.ge("create_time", startTime);
        }
        if (StringUtil.isNotBlank(endTime)) {
            wrapper.le("create_time", endTime + " 23:59:59");
        }
        if (projectId != null) {
            wrapper.eq("project_id", projectId);
        }
        
        wrapper.orderByDesc("create_time");
        
        return salesOrderMapper.selectPage(page, wrapper);
    }

    @Override
    public Map<String, Object> getOrderDetail(Long id) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取订单主表信息
        SalesOrder salesOrder = salesOrderMapper.selectById(id);
        result.put("order", salesOrder);
        
        // 获取订单明细
        QueryWrapper<SalesOrderLine> lineWrapper = new QueryWrapper<>();
        lineWrapper.eq("order_id", id);
        List<SalesOrderLine> lines = salesOrderLineMapper.selectList(lineWrapper);
        result.put("lines", lines);
        
        // 获取应收款单（如果有）- 将receivables改为receivable（与前端期望一致）
        QueryWrapper<ArReceivable> receivableWrapper = new QueryWrapper<>();
        receivableWrapper.eq("order_id", id);
        List<ArReceivable> receivables = arReceivableMapper.selectList(receivableWrapper);
        if (!receivables.isEmpty()) {
            // 前端期望单个应收单对象，不是列表
            result.put("receivable", receivables.get(0));
        }
        
        // 获取会计分录（如果有）- 将entries改为accountingEntries（与前端期望一致）
        QueryWrapper<JournalEntry> entryWrapper = new QueryWrapper<>();
        entryWrapper.eq("source_id", id).eq("source_type", "SALES");
        List<JournalEntry> entries = journalEntryMapper.selectList(entryWrapper);
        // 前端期望的键名是accountingEntries
        result.put("accountingEntries", entries);
        
        return result;
    }

    @Override
    public SalesOrder getByOrderNo(String orderNo) {
        QueryWrapper<SalesOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("order_no", orderNo);
        return salesOrderMapper.selectOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateStatus(List<Long> ids, String status) {
        try {
            SalesOrder salesOrder = new SalesOrder();
            salesOrder.setStatus(status);
            
            QueryWrapper<SalesOrder> wrapper = new QueryWrapper<>();
            wrapper.in("id", ids);
            
            return salesOrderMapper.update(salesOrder, wrapper) > 0;
        } catch (Exception e) {
            log.error("批量更新销售订单状态失败: {}", e.getMessage(), e);
            throw new RuntimeException("批量更新销售订单状态失败: " + e.getMessage());
        }
    }

    // 生成订单编号
    private String generateOrderNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String prefix = "SO" + sdf.format(new Date());
        QueryWrapper<SalesOrder> wrapper = new QueryWrapper<>();
        wrapper.likeRight("order_no", prefix);
        wrapper.orderByDesc("order_no");
        wrapper.last("LIMIT 1");
        
        SalesOrder lastOrder = salesOrderMapper.selectOne(wrapper);
        if (lastOrder == null) {
            return prefix + "001";
        } else {
            String lastNo = lastOrder.getOrder_no();
            int seq = Integer.parseInt(lastNo.substring(10)) + 1;
            return String.format("%s%03d", prefix, seq);
        }
    }

    // 生成应收款单号
    private String generateReceivableNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String prefix = "AR" + sdf.format(new Date());
        QueryWrapper<ArReceivable> wrapper = new QueryWrapper<>();
        wrapper.likeRight("receivable_no", prefix);
        wrapper.orderByDesc("receivable_no");
        wrapper.last("LIMIT 1");
        
        ArReceivable lastReceivable = arReceivableMapper.selectOne(wrapper);
        if (lastReceivable == null) {
            return prefix + "001";
        } else {
            String lastNo = lastReceivable.getReceivable_no();
            int seq = Integer.parseInt(lastNo.substring(8)) + 1;
            return String.format("%s%03d", prefix, seq);
        }
    }

    // 计算订单总金额（包含税额）
    private BigDecimal calculateTotalAmount(List<SalesOrderLine> lines) {
        BigDecimal total = BigDecimal.ZERO;
        for (SalesOrderLine line : lines) {
            // 添加null检查
            if (line.getQuantity() == null) {
                line.setQuantity(BigDecimal.ZERO);
            }
            if (line.getPrice() == null) {
                line.setPrice(BigDecimal.ZERO);
            }
            if (line.getTax_rate() == null) {
                line.setTax_rate(BigDecimal.ZERO);
            }
            // 计算不含税金额
            BigDecimal lineAmount = line.getQuantity().multiply(line.getPrice());
            // 计算税额
            BigDecimal taxAmount = lineAmount.multiply(line.getTax_rate()).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP);
            // 总金额 = 不含税金额 + 税额
            total = total.add(lineAmount).add(taxAmount);
        }
        return total;
    }

    // 保存订单明细
    private void saveOrderLines(List<SalesOrderLine> lines, Long orderId) {
        for (SalesOrderLine line : lines) {
            line.setOrder_id(orderId);
            // 计算不含税金额
            BigDecimal lineAmount = line.getQuantity().multiply(line.getPrice());
            // 计算税额
            BigDecimal taxAmount = lineAmount.multiply(line.getTax_rate()).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP);
            
            line.setAmount(lineAmount);
            line.setTax_amount(taxAmount);
            line.setCreate_time(new Date());
            salesOrderLineMapper.insert(line);
        }
    }

    // 生成会计分录
    private void generateAccountingEntries(SalesOrder salesOrder, ArReceivable receivable) {
        try {
            log.info("开始生成销售订单会计分录，订单ID: {}", salesOrder.getId());
            log.info("销售订单业务子类型: {}", salesOrder.getBiz_sub_type());
            
            // 获取科目映射，使用销售订单的业务子类型
            BizAccountMapping salesMapping = getAccountMapping("SALES", salesOrder.getBiz_sub_type());
            
            if (salesMapping == null) {
                log.warn("未找到【{}-{}】的科目映射规则，尝试查询默认的服务销售映射", "SALES", salesOrder.getBiz_sub_type());
                // 如果没有找到映射规则，尝试使用默认的服务销售映射
                salesMapping = getAccountMapping("SALES", "服务销售");
                if (salesMapping == null) {
                    log.error("未找到销售业务科目映射配置");
                    throw new RuntimeException("缺少销售业务科目映射配置");
                }
            }
            log.info("找到科目映射: 借方科目代码={}, 贷方科目代码={}", salesMapping.getDebit_code(), salesMapping.getCredit_code());
            
            // 获取科目名称
            String debitName = getSubjectName(salesMapping.getDebit_code());
            String creditName = getSubjectName(salesMapping.getCredit_code());
            log.info("科目名称: 借方={}, 贷方={}", debitName, creditName);
            
            // 查询销项税科目
            AccountingSubject taxSubject = accountingSubjectMapper.selectOne(new QueryWrapper<AccountingSubject>().eq("subject_code", "22210102"));
            if (taxSubject == null) {
                log.error("未找到销项税科目：22210102");
                throw new RuntimeException("未找到销项税科目：22210102");
            }
            log.info("销项税科目: {}-{}", "22210102", taxSubject.getSubjectName());
            
            // 汇总订单项金额和税额
            QueryWrapper<SalesOrderLine> lineWrapper = new QueryWrapper<>();
            lineWrapper.eq("order_id", salesOrder.getId());
            List<SalesOrderLine> lines = salesOrderLineMapper.selectList(lineWrapper);
            log.info("订单项数量: {}", lines.size());
            
            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal totalTax = BigDecimal.ZERO;
            for (SalesOrderLine line : lines) {
                totalAmount = totalAmount.add(line.getAmount()); // 不含税金额
                totalTax = totalTax.add(line.getTax_amount()); // 税额
            }
            log.info("汇总金额: 不含税={}, 税额={}", totalAmount, totalTax);
            
            // 创建主分录（不含税金额）
            JournalEntry mainEntry = new JournalEntry();
            mainEntry.setSource_type("SALES");
            mainEntry.setSource_id(salesOrder.getId());
            mainEntry.setDebit_code(salesMapping.getDebit_code());
            mainEntry.setDebit_name(debitName);
            mainEntry.setCredit_code(salesMapping.getCredit_code());
            mainEntry.setCredit_name(creditName);
            mainEntry.setAmount(totalAmount);
            mainEntry.setStatus("待过账"); // 自动生成的分录初始状态为待过账
            mainEntry.setRemark("销售订单 " + salesOrder.getOrder_no() + " 生成应收款单 " + receivable.getReceivable_no());
            mainEntry.setCreate_user_id(receivable.getCreate_user_id());
            mainEntry.setCreate_time(new Date());
            mainEntry.setUpdate_time(new Date());
            
            journalEntryMapper.insert(mainEntry);
            log.info("主分录已生成: {}", mainEntry);
            
            // 创建销项税分录
            JournalEntry taxEntry = new JournalEntry();
            taxEntry.setSource_type("SALES");
            taxEntry.setSource_id(salesOrder.getId());
            taxEntry.setDebit_code(salesMapping.getDebit_code());
            taxEntry.setDebit_name(debitName);
            taxEntry.setCredit_code("22210102"); // 销项税科目
            taxEntry.setCredit_name(taxSubject.getSubjectName());
            taxEntry.setAmount(totalTax);
            taxEntry.setTax_amount(totalTax);
            taxEntry.setStatus("待过账"); // 自动生成的分录初始状态为待过账
            taxEntry.setRemark("销售订单 " + salesOrder.getOrder_no() + " 生成应收款单 " + receivable.getReceivable_no() + " (销项税)");
            taxEntry.setCreate_user_id(receivable.getCreate_user_id());
            taxEntry.setCreate_time(new Date());
            taxEntry.setUpdate_time(new Date());
            
            journalEntryMapper.insert(taxEntry);
            log.info("销项税分录已生成: {}", taxEntry);
            log.info("销售订单会计分录生成完成");
        } catch (Exception e) {
            log.error("生成销售订单会计分录失败: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    // 获取科目名称
    private String getSubjectName(String subjectCode) {
        QueryWrapper<AccountingSubject> wrapper = new QueryWrapper<>();
        wrapper.eq("subject_code", subjectCode);
        AccountingSubject subject = accountingSubjectMapper.selectOne(wrapper);
        return subject != null ? subject.getSubjectName() : "";
    }

    // 获取科目映射
    private BizAccountMapping getAccountMapping(String bizType, String subType) {
        // 使用mapper提供的自定义查询方法
        return bizAccountMappingMapper.selectByBizTypeAndSubType(bizType, subType);
    }


}