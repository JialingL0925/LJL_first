package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.mapper.AccountingSubjectMapper;
import com.example.demo.model.entity.AccountingSubject;
import com.example.demo.service.AccountingSubjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * 会计科目Service实现
 */
@Service
public class AccountingSubjectServiceImpl extends ServiceImpl<AccountingSubjectMapper, AccountingSubject>
        implements AccountingSubjectService {

    @Override
    public IPage<AccountingSubject> pageList(Page<AccountingSubject> page,
                                             String subjectName,
                                             String status,
                                             String subjectType) {
        LambdaQueryWrapper<AccountingSubject> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(subjectName)) {
            wrapper.like(AccountingSubject::getSubjectName, subjectName);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(AccountingSubject::getStatus, status);
        }
        if (StringUtils.hasText(subjectType)) {
            wrapper.eq(AccountingSubject::getSubjectType, subjectType);
        }
        wrapper.orderByAsc(AccountingSubject::getSubjectLevel, AccountingSubject::getSubjectCode);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public List<AccountingSubject> treeList(Long parentSubjectId) {
        LambdaQueryWrapper<AccountingSubject> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccountingSubject::getParentSubjectId, parentSubjectId)
                .orderByAsc(AccountingSubject::getSubjectCode);
        return baseMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changeStatus(Long subjectId, String status, String deactivationReason) {
        LambdaUpdateWrapper<AccountingSubject> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AccountingSubject::getSubjectId, subjectId)
                .set(AccountingSubject::getStatus, status)
                .set(AccountingSubject::getDeactivationReason, deactivationReason)
                .set(AccountingSubject::getUpdateTime, new Date());
        return update(wrapper);
    }

    @Override
    public boolean save(AccountingSubject entity) {
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        return super.save(entity);
    }

    @Override
    public boolean updateById(AccountingSubject entity) {
        entity.setUpdateTime(new Date());
        return super.updateById(entity);
    }
}