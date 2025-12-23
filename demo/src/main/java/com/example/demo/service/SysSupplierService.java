package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.model.entity.SysSupplier;

/**
 * 供应商Service接口
 */
public interface SysSupplierService extends IService<SysSupplier> {
    // 继承IService后，自动拥有分页查询、批量删除等高级方法
}