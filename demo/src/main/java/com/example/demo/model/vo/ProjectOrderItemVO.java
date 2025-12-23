package com.example.demo.model.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 项目订单明细VO（用于项目报表）
 */
@Data
public class ProjectOrderItemVO {
    private Long orderId;           // 订单ID
    private String orderNo;         // 订单编号
    private String orderType;       // 订单类型（采购/销售）
    private BigDecimal amount;      // 订单金额
    private String status;          // 订单状态
    private Date createTime;        // 创建时间
    private String supplierName;   // 供应商名称（采购订单）
    private String customerName;    // 客户名称（销售订单）
}

