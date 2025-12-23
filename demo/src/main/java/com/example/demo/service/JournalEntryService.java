package com.example.demo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.model.entity.JournalEntry;

import java.util.List;

// 绿色接口文件，对应JournalEntryServiceImpl的实现
public interface JournalEntryService extends IService<JournalEntry> {
    boolean saveManualEntry(JournalEntry entry);
    IPage<JournalEntry> pageList(Page<JournalEntry> page, String entryNo, String sourceType, Long sourceId, String status, String entryDateStart, String entryDateEnd);
    List<JournalEntry> getBySource(String sourceType, Long sourceId);
    
    /**
     * 过账单个分录
     * @param id 分录ID
     * @param userId 过账人ID
     * @return 是否成功
     */
    boolean postEntry(Long id, Long userId);
    
    /**
     * 批量过账分录
     * @param ids 分录ID列表
     * @param userId 过账人ID
     * @return 是否成功
     */
    boolean batchPostEntry(List<Long> ids, Long userId);
}