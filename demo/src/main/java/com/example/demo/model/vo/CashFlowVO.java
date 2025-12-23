package com.example.demo.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 现金流量表汇总 VO（简化版）
 */
@Data
public class CashFlowVO {

    /**
     * 报表期间，例如：2025-12-01 至 2025-12-31
     */
    private String period;

    /**
     * 明细项目列表
     */
    private List<CashFlowItemVO> items = new ArrayList<>();

    /**
     * 经营活动产生的现金流量净额
     */
    private BigDecimal netOperating = BigDecimal.ZERO;

    /**
     * 投资活动产生的现金流量净额
     */
    private BigDecimal netInvesting = BigDecimal.ZERO;

    /**
     * 筹资活动产生的现金流量净额
     */
    private BigDecimal netFinancing = BigDecimal.ZERO;

    /**
     * 现金及现金等价物净增加额
     */
    private BigDecimal netIncrease = BigDecimal.ZERO;
}


