package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.mapper.*;
import com.example.demo.model.entity.*;
import com.example.demo.service.ApPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 付款单Service实现类
 */
@Service
public class ApPaymentServiceImpl extends ServiceImpl<ApPaymentMapper, ApPayment> implements ApPaymentService {
    
    private static final AtomicInteger PAYMENT_SEQ = new AtomicInteger(1);
    
    @Autowired
    private ApPaymentMapper apPaymentMapper;
    
    @Autowired
    private ApPayableMapper apPayableMapper;
    
    @Autowired
    private BizAccountMappingMapper bizAccountMappingMapper;
    
    @Autowired
    private AccountingSubjectMapper accountingSubjectMapper;
    
    @Autowired
    private JournalEntryMapper journalEntryMapper;
    
    @Override
    public IPage<ApPayment> pageList(Page<ApPayment> page, String paymentNo, Long payableId, Long supplierId, String status, String paymentDateStart, String paymentDateEnd) {
        LambdaQueryWrapper<ApPayment> queryWrapper = new LambdaQueryWrapper<>();
        
        if (paymentNo != null && !paymentNo.isEmpty()) {
            queryWrapper.like(ApPayment::getPayment_no, paymentNo);
        }
        if (payableId != null) {
            queryWrapper.eq(ApPayment::getPayable_id, payableId);
        }
        if (supplierId != null) {
            queryWrapper.eq(ApPayment::getSupplier_id, supplierId);
        }
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(ApPayment::getStatus, status);
        }
        if (paymentDateStart != null && !paymentDateStart.isEmpty()) {
            queryWrapper.ge(ApPayment::getPayment_date, paymentDateStart);
        }
        if (paymentDateEnd != null && !paymentDateEnd.isEmpty()) {
            queryWrapper.le(ApPayment::getPayment_date, paymentDateEnd);
        }
        
        queryWrapper.orderByDesc(ApPayment::getCreate_time);
        
        return apPaymentMapper.selectPage(page, queryWrapper);
    }
    
    @Override
    public ApPayment getById(Long id) {
        return apPaymentMapper.selectById(id);
    }
    
    @Override
    public boolean createPayment(ApPayment payment) {
        // 生成付款单编号
        String paymentNo = generatePaymentNo();
        payment.setPayment_no(paymentNo);
        payment.setCreate_time(new Date());
        payment.setStatus("待确认");
        
        return apPaymentMapper.insert(payment) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmPayment(Long id, Long userId) {
        try {
            // 1. 查询付款单
            ApPayment payment = apPaymentMapper.selectById(id);
            if (payment == null) {
                throw new RuntimeException("付款单不存在");
            }
            
            if (!"待确认".equals(payment.getStatus())) {
                throw new RuntimeException("付款单状态非待确认，无法确认");
            }
            
            // 2. 更新付款单状态
            payment.setStatus("已确认");
            apPaymentMapper.updateById(payment);
            
            // 3. 更新关联应付单状态为已核销
            ApPayable payable = apPayableMapper.selectById(payment.getPayable_id());
            if (payable != null) {
                payable.setStatus("已核销");
                apPayableMapper.updateById(payable);
            }
            
            // 4. 生成付款凭证分录
            generatePaymentJournalEntry(payment);
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("确认付款失败：" + e.getMessage());
        }
    }
    
    @Override
    public boolean cancelPayment(Long id) {
        ApPayment payment = apPaymentMapper.selectById(id);
        if (payment == null) {
            return false;
        }
        
        if ("待确认".equals(payment.getStatus())) {
            payment.setStatus("已取消");
            return apPaymentMapper.updateById(payment) > 0;
        }
        
        return false;
    }
    
    /**
     * 生成付款单编号
     */
    private String generatePaymentNo() {
        String dateStr = new java.text.SimpleDateFormat("yyyyMMdd").format(new Date());
        int seq = PAYMENT_SEQ.getAndIncrement();
        if (seq >= 10000) {
            PAYMENT_SEQ.set(1);
        }
        return "PAY" + dateStr + String.format("%04d", seq);
    }
    
    /**
     * 生成付款凭证分录
     */
    private void generatePaymentJournalEntry(ApPayment payment) {
        try {
            // 查询付款业务的科目映射规则
            BizAccountMapping mapping = bizAccountMappingMapper.selectByBizTypeAndSubType("PAYMENT", "供应商付款");
            if (mapping == null) {
                throw new RuntimeException("未配置付款业务的科目映射规则");
            }
            
            // 查询借方科目（应付账款）
            AccountingSubject debitSubject = accountingSubjectMapper.selectByCode(mapping.getDebit_code());
            if (debitSubject == null) {
                throw new RuntimeException("未找到借方科目：" + mapping.getDebit_code());
            }
            
            // 根据付款方式选择不同的贷方科目
            String creditCode;
            if ("现金".equals(payment.getPayment_method())) {
                creditCode = "1001"; // 库存现金科目
            } else {
                creditCode = "1002"; // 银行存款科目（包括银行转账和支票付款）
            }
            
            // 查询贷方科目
            AccountingSubject creditSubject = accountingSubjectMapper.selectByCode(creditCode);
            if (creditSubject == null) {
                throw new RuntimeException("未找到贷方科目：" + creditCode);
            }
            
            // 生成主分录（应付账款减少）
            JournalEntry mainEntry = new JournalEntry();
            mainEntry.setSource_type("payment");
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
            throw new RuntimeException("生成付款凭证分录失败：" + e.getMessage());
        }
    }
}