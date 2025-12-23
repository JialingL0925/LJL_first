package com.example.demo.model.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 现金流量表项目 VO（简化版）
 */
@Data
public class CashFlowItemVO {

    /**
     * 项目名称，例如：销售收现、采购付现等
     */
    private String itemName;

    /**
     * 所属类别：经营活动 / 投资活动 / 筹资活动
     */
    private String category;

    /**
     * 本期金额（流入为正，流出为负）
     */
    private BigDecimal amount;
}


