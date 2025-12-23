package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.model.entity.ApPayment;
import org.springframework.stereotype.Repository;

/**
 * 付款单Mapper接口
 */
@Repository
public interface ApPaymentMapper extends BaseMapper<ApPayment> {
    // 可以在这里添加自定义的查询方法
}