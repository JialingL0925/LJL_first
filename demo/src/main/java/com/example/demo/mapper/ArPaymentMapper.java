package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.model.entity.ArPayment;
import org.springframework.stereotype.Repository;

/**
 * 收款单Mapper接口
 */
@Repository
public interface ArPaymentMapper extends BaseMapper<ArPayment> {
    // 可以在这里添加自定义的查询方法
}