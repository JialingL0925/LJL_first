package com.example.demo.model.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 利润表VO
 */
@Data
public class ProfitLossVO {
    private String period;              // 报表期间（如：2025-12）
    private BigDecimal revenue;         // 营业收入
    private BigDecimal costOfSales;     // 营业成本
    private BigDecimal grossProfit;     // 毛利润
    private BigDecimal operatingExpenses; // 营业费用
    private BigDecimal operatingProfit;  // 营业利润
    private BigDecimal netProfit;       // 净利润
    private List<ProfitLossItemVO> items; // 明细项
}





