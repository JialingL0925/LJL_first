package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.model.entity.JournalEntry;
import com.example.demo.mapper.JournalEntryMapper;
import com.example.demo.service.JournalEntryService;
import java.util.Map;
import com.example.demo.model.entity.Employee;
import com.example.demo.mapper.EmployeeMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Date;
import java.util.List;

@Service
public class JournalEntryServiceImpl extends ServiceImpl<JournalEntryMapper, JournalEntry> implements JournalEntryService {

    @Autowired
    private EmployeeMapper employeeMapper;
    
    @Autowired
    private JournalEntryMapper journalEntryMapper;

    @Override
    public boolean saveManualEntry(JournalEntry entry) {
        entry.setSource_type("MANUAL"); // source_type
        entry.setSource_id(0L); // source_id
        entry.setStatus("待过账"); // 手动录入的分录初始状态为待过账
        entry.setCreate_time(new Date()); // create_time
        
        // 设置创建人ID
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                String phone = userDetails.getUsername(); // 手机号作为用户名
                
                // 根据手机号查询员工信息
                Employee employee = employeeMapper.selectByPhone(phone);
                if (employee != null) {
                    entry.setCreate_user_id(employee.getEmployeeId()); // 设置正确的创建人ID
                }
            }
        } catch (Exception e) {
            // 记录日志或其他处理
            e.printStackTrace();
        }
        
        return this.save(entry);
    }

    @Override
    public IPage<JournalEntry> pageList(Page<JournalEntry> page, String entryNo, String sourceType, Long sourceId, String status, String entryDateStart, String entryDateEnd) {
        LambdaQueryWrapper<JournalEntry> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (entryNo != null && !entryNo.isEmpty()) {
            queryWrapper.like(JournalEntry::getId, entryNo); // 假设entryNo对应id
        }
        if (sourceType != null && !sourceType.isEmpty()) {
            // 统一转换为大写以匹配数据库存储格式
            sourceType = sourceType.toUpperCase();
            // 根据前端传递的sourceType映射到后端存储的值
            if ("PURCHASE_ORDER".equals(sourceType)) {
                sourceType = "PURCHASE";
            }
            queryWrapper.eq(JournalEntry::getSource_type, sourceType);
        }
        if (sourceId != null) {
            queryWrapper.eq(JournalEntry::getSource_id, sourceId);
        }
        if (status != null && !status.isEmpty()) {
            // 根据前端传入的状态值进行查询
            queryWrapper.eq(JournalEntry::getStatus, status);
        }
        // 按创建时间降序排列
        queryWrapper.orderByDesc(JournalEntry::getCreate_time);
        
        // 执行查询
        IPage<JournalEntry> result = this.page(page, queryWrapper);
        
        // 调试日志：打印查询结果
        if (result != null && result.getRecords() != null && !result.getRecords().isEmpty()) {
            JournalEntry firstEntry = result.getRecords().get(0);
            System.out.println("🔍 [JournalEntryService] 查询结果 - 总数: " + result.getTotal());
            System.out.println("🔍 [JournalEntryService] 第一条数据ID: " + firstEntry.getId() + ", 状态: '" + firstEntry.getStatus() + "'");
            System.out.println("🔍 [JournalEntryService] 查询条件 - status参数: " + status);
            System.out.println("🔍 [JournalEntryService] Status字段类型: " + (firstEntry.getStatus() != null ? firstEntry.getStatus().getClass().getName() : "null"));
            System.out.println("🔍 [JournalEntryService] Status值长度: " + (firstEntry.getStatus() != null ? firstEntry.getStatus().length() : 0));
            
            // 使用原生SQL直接查询对比
            try {
                Map<String, Object> rawData = journalEntryMapper.selectRawById(firstEntry.getId());
                if (rawData != null) {
                    Object rawStatus = rawData.get("status");
                    System.out.println("🔍 [JournalEntryService] 原生SQL查询的status: '" + rawStatus + "'");
                    System.out.println("🔍 [JournalEntryService] 原生SQL status类型: " + (rawStatus != null ? rawStatus.getClass().getName() : "null"));
                    System.out.println("🔍 [JournalEntryService] 两者是否匹配: " + (firstEntry.getStatus() != null && firstEntry.getStatus().equals(rawStatus)));
                }
            } catch (Exception e) {
                System.out.println("🔍 [JournalEntryService] 原生SQL查询失败: " + e.getMessage());
            }
        }
        
        return result;
    }

    @Override
    public List<JournalEntry> getBySource(String sourceType, Long sourceId) {
        LambdaQueryWrapper<JournalEntry> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(JournalEntry::getSource_type, sourceType);
        queryWrapper.eq(JournalEntry::getSource_id, sourceId);
        return this.list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean postEntry(Long id, Long userId) {
        // 1. 查询分录
        JournalEntry entry = this.getById(id);
        if (entry == null) {
            throw new RuntimeException("分录不存在，ID: " + id);
        }

        // 2. 检查状态：只有"待过账"或"已生效"状态的凭证可以过账
        String currentStatus = entry.getStatus();
        if ("已过账".equals(currentStatus)) {
            throw new RuntimeException("该分录已经过账，不能重复过账");
        }
        if ("已作废".equals(currentStatus) || "无效".equals(currentStatus)) {
            throw new RuntimeException("已作废或无效的分录不能过账");
        }

        // 3. 更新状态和过账信息
        entry.setStatus("已过账");
        entry.setPost_user_id(userId);
        entry.setPost_time(new Date());

        // 4. 保存更新
        return this.updateById(entry);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchPostEntry(List<Long> ids, Long userId) {
        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("分录ID列表不能为空");
        }

        // 批量查询分录
        List<JournalEntry> entries = this.listByIds(ids);
        if (entries.size() != ids.size()) {
            throw new RuntimeException("部分分录不存在");
        }

        Date postTime = new Date();
        // 批量更新
        for (JournalEntry entry : entries) {
            String currentStatus = entry.getStatus();
            if ("已过账".equals(currentStatus)) {
                throw new RuntimeException("分录ID " + entry.getId() + " 已经过账，不能重复过账");
            }
            if ("已作废".equals(currentStatus) || "无效".equals(currentStatus)) {
                throw new RuntimeException("分录ID " + entry.getId() + " 已作废或无效，不能过账");
            }

            entry.setStatus("已过账");
            entry.setPost_user_id(userId);
            entry.setPost_time(postTime);
        }

        // 批量更新
        return this.updateBatchById(entries);
    }
}