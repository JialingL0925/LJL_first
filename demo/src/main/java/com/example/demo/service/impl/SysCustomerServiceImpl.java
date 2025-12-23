package com.example.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.model.entity.SysCustomer;
import com.example.demo.mapper.SysCustomerMapper;
import com.example.demo.service.SysCustomerService;
import org.springframework.stereotype.Service;
import java.util.Date;

/**
 * 客户Service实现（手动维护时间字段，兼容低版本数据库）
 */
@Service
public class SysCustomerServiceImpl extends ServiceImpl<SysCustomerMapper, SysCustomer> implements SysCustomerService {

    /**
     * 新增客户时自动填充创建时间和更新时间
     */
    @Override
    public boolean save(SysCustomer customer) {
        Date now = new Date();
        // 强制设置创建时间（避免前端未传）
        if (customer.getCreateTime() == null) {
            customer.setCreateTime(now);
        }
        // 新增时更新时间与创建时间一致
        if (customer.getUpdateTime() == null) {
            customer.setUpdateTime(now);
        }
        return super.save(customer);
    }

    /**
     * 更新客户时自动刷新更新时间
     */
    @Override
    public boolean updateById(SysCustomer customer) {
        // 强制更新时间为当前时间（覆盖前端传入值）
        customer.setUpdateTime(new Date());
        return super.updateById(customer);
    }
}