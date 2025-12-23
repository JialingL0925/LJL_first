package com.example.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.model.entity.SysSupplier;
import com.example.demo.mapper.SysSupplierMapper;
import com.example.demo.service.SysSupplierService;
import org.springframework.stereotype.Service;

/**
 * 供应商Service实现类
 */
@Service // 标记为Spring的Service组件
public class SysSupplierServiceImpl extends ServiceImpl<SysSupplierMapper, SysSupplier> implements SysSupplierService {
    // 暂时无需自定义方法
}