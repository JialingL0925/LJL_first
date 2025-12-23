package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.model.entity.SysCustomer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户Mapper（继承BaseMapper，与实体类绑定）
 */
@Mapper
public interface SysCustomerMapper extends BaseMapper<SysCustomer> {
}