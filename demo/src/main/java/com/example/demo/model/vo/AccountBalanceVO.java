package com.example.demo.model.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 科目余额表VO
 */
@Data
public class AccountBalanceVO {
    private String subjectCode;      // 科目编码
    private String subjectName;      // 科目名称
    private String subjectType;     // 科目类型
    private BigDecimal beginBalance; // 期初余额
    private BigDecimal debitAmount;  // 本期借方发生额
    private BigDecimal creditAmount; // 本期贷方发生额
    private BigDecimal endBalance;   // 期末余额
}





