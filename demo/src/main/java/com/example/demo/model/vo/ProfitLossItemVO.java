package com.example.demo.model.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 利润表明细项VO
 */
@Data
public class ProfitLossItemVO {
    private String subjectCode;    // 科目编码
    private String subjectName;     // 科目名称
    private BigDecimal amount;     // 金额
    private String itemType;       // 项目类型（收入/成本/费用）
}




