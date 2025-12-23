package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.mapper.*;
import com.example.demo.model.entity.*;
import com.example.demo.service.ArPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 收款单Service实现类
 */
@Service
public class ArPaymentServiceImpl extends ServiceImpl<ArPaymentMapper, ArPayment> implements ArPaymentService {
    
    @Autowired
    private ArPaymentMapper arPaymentMapper;
    
    @Autowired
    private ArReceivableMapper arReceivableMapper;
    
    @Autowired
    private BizAccountMappingMapper bizAccountMappingMapper;
    
    @Autowired
    private AccountingSubjectMapper accountingSubjectMapper;
    
    @Autowired
    private JournalEntryMapper journalEntryMapper;
    
    @Override
    public IPage<ArPayment> pageList(Page<ArPayment> page, String paymentNo, Long receivableId, Long customerId, String status, String paymentDateStart, String paymentDateEnd) {
        LambdaQueryWrapper<ArPayment> queryWrapper = new LambdaQueryWrapper<>();
        
        if (paymentNo != null && !paymentNo.isEmpty()) {
            queryWrapper.like(ArPayment::getPayment_no, paymentNo);
        }
        if (receivableId != null) {
            queryWrapper.eq(ArPayment::getReceivable_id, receivableId);
        }
        if (customerId != null) {
            queryWrapper.eq(ArPayment::getCustomer_id, customerId);
        }
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(ArPayment::getStatus, status);
        }
        if (paymentDateStart != null && !paymentDateStart.isEmpty()) {
            queryWrapper.ge(ArPayment::getPayment_date, paymentDateStart);
        }
        if (paymentDateEnd != null && !paymentDateEnd.isEmpty()) {
            queryWrapper.le(ArPayment::getPayment_date, paymentDateEnd);
        }
        
        queryWrapper.orderByDesc(ArPayment::getCreate_time);
        
        return arPaymentMapper.selectPage(page, queryWrapper);
    }
    
    @Override
    public ArPayment getById(Long id) {
        return arPaymentMapper.selectById(id);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createPayment(ArPayment payment) {
        // 生成收款单编号（带重试机制，确保唯一性）
        String paymentNo = generateUniquePaymentNo();
        payment.setPayment_no(paymentNo);
        payment.setCreate_time(new Date());
        payment.setStatus("待确认");
        
        return arPaymentMapper.insert(payment) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmPayment(Long id, Long userId) {
        try {
            // 1. 查询收款单
            ArPayment payment = arPaymentMapper.selectById(id);
            if (payment == null) {
                throw new RuntimeException("收款单不存在");
            }
            
            if (!"待确认".equals(payment.getStatus())) {
                throw new RuntimeException("收款单状态非待确认，无法确认");
            }
            
            // 2. 更新收款单状态
            payment.setStatus("已确认");
            arPaymentMapper.updateById(payment);
            
            // 3. 更新关联应收单状态为已核销
            ArReceivable receivable = arReceivableMapper.selectById(payment.getReceivable_id());
            if (receivable != null) {
                receivable.setStatus("已核销");
                arReceivableMapper.updateById(receivable);
            }
            
            // 4. 生成收款凭证分录
            generateReceiptJournalEntry(payment);
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("确认收款失败：" + e.getMessage());
        }
    }
    
    @Override
    public boolean cancelPayment(Long id) {
        ArPayment payment = arPaymentMapper.selectById(id);
        if (payment == null) {
            return false;
        }
        
        if ("待确认".equals(payment.getStatus())) {
            payment.setStatus("已取消");
            return arPaymentMapper.updateById(payment) > 0;
        }
        
        return false;
    }
    
    /**
     * 生成收款单编号（从数据库查询当天最大序号，确保唯一性）
     */
    private String generateUniquePaymentNo() {
        String dateStr = new java.text.SimpleDateFormat("yyyyMMdd").format(new Date());
        String prefix = "REC" + dateStr;
        
        // 查询当天最大的收款单号
        LambdaQueryWrapper<ArPayment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.likeRight(ArPayment::getPayment_no, prefix)
                .orderByDesc(ArPayment::getPayment_no)
                .last("LIMIT 1");
        
        ArPayment lastPayment = arPaymentMapper.selectOne(queryWrapper);
        
        int seq = 1;
        if (lastPayment != null && lastPayment.getPayment_no() != null) {
            String lastNo = lastPayment.getPayment_no();
            // 提取序号部分（REC + 日期 + 4位序号）
            if (lastNo.startsWith(prefix) && lastNo.length() == prefix.length() + 4) {
                try {
                    String seqStr = lastNo.substring(prefix.length());
                    seq = Integer.parseInt(seqStr) + 1;
                    // 确保序号不超过9999
                    if (seq > 9999) {
                        seq = 1;
                    }
                } catch (NumberFormatException e) {
                    // 如果解析失败，从1开始
                    seq = 1;
                }
            }
        }
        
        return prefix + String.format("%04d", seq);
    }
    
    /**
     * 生成收款凭证分录
     */
    private void generateReceiptJournalEntry(ArPayment payment) {
        try {
            // 根据收款方式选择不同的借方科目
            String debitCode;
            if ("现金".equals(payment.getPayment_method())) {
                debitCode = "1001"; // 库存现金科目
            } else {
                debitCode = "1002"; // 银行存款科目（包括银行转账）
            }
            
            // 查询借方科目（库存现金或银行存款）
            AccountingSubject debitSubject = accountingSubjectMapper.selectByCode(debitCode);
            if (debitSubject == null) {
                throw new RuntimeException("未找到借方科目：" + debitCode);
            }
            
            // 收款的贷方科目是应收账款
            String creditCode = "1122"; // 应收账款科目
            
            // 查询贷方科目（应收账款）
            AccountingSubject creditSubject = accountingSubjectMapper.selectByCode(creditCode);
            if (creditSubject == null) {
                throw new RuntimeException("未找到贷方科目：" + creditCode);
            }
            
            // 生成主分录（应收账款减少）
            JournalEntry mainEntry = new JournalEntry();
            mainEntry.setSource_type("receipt");
            mainEntry.setSource_id(payment.getId());
            mainEntry.setDebit_code(debitSubject.getSubjectCode());
            mainEntry.setDebit_name(debitSubject.getSubjectName());
            mainEntry.setCredit_code(creditSubject.getSubjectCode());
            mainEntry.setCredit_name(creditSubject.getSubjectName());
            mainEntry.setAmount(payment.getPayment_amount());
            mainEntry.setStatus("待过账"); // 自动生成的分录初始状态为待过账
            mainEntry.setCreate_user_id(payment.getCreate_user_id());
            mainEntry.setCreate_time(new Date());
            
            journalEntryMapper.insert(mainEntry);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("生成收款凭证分录失败：" + e.getMessage());
        }
    }
}