package com.example.demo.model.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 资产负债表VO
 */
@Data
public class BalanceSheetVO {
    private String reportDate;              // 报表日期
    private List<BalanceSheetItemVO> assets; // 资产项
    private List<BalanceSheetItemVO> liabilities; // 负债项
    private List<BalanceSheetItemVO> equity; // 所有者权益项
    private BigDecimal totalAssets;         // 资产总计
    private BigDecimal totalLiabilities;    // 负债总计
    private BigDecimal totalEquity;         // 所有者权益总计
}





