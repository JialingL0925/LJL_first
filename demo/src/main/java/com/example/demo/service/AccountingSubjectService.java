package com.example.demo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.model.entity.AccountingSubject;

import java.util.List;

/**
 * 会计科目Service接口
 */
public interface AccountingSubjectService extends IService<AccountingSubject> {
    /**
     * 分页查询
     */
    IPage<AccountingSubject> pageList(Page<AccountingSubject> page,
                                      String subjectName,
                                      String status,
                                      String subjectType);

    /**
     * 查询树形结构
     */
    List<AccountingSubject> treeList(Long parentSubjectId);

    /**
     * 切换科目状态
     */
    boolean changeStatus(Long subjectId, String status, String deactivationReason);
}