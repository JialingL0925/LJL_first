package com.example.demo.model.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 项目报表VO
 */
@Data
public class ProjectReportVO {
    // 项目基本信息
    private Long projectId;              // 项目ID
    private String projectCode;          // 项目编号
    private String projectName;          // 项目名称
    private String projectManager;       // 项目经理
    private Date startDate;              // 开始日期
    private Date endDate;                // 结束日期
    private BigDecimal budget;          // 预算金额
    private String status;               // 项目状态
    
    // 采购订单统计
    private Integer purchaseOrderCount;  // 采购订单数量
    private BigDecimal purchaseAmount;  // 采购订单总金额
    
    // 销售订单统计
    private Integer salesOrderCount;     // 销售订单数量
    private BigDecimal salesAmount;     // 销售订单总金额
    
    // 收支情况
    private BigDecimal totalIncome;     // 总收入（销售订单金额）
    private BigDecimal totalExpense;    // 总支出（采购订单金额）
    private BigDecimal netProfit;       // 净利润（收入-支出）
    private BigDecimal budgetUtilization; // 预算使用率（支出/预算）
    
    // 订单明细列表
    private List<ProjectOrderItemVO> purchaseOrders; // 采购订单列表
    private List<ProjectOrderItemVO> salesOrders;    // 销售订单列表
}

