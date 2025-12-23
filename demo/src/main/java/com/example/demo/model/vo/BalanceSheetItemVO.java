package com.example.demo.model.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 资产负债表项目VO
 */
@Data
public class BalanceSheetItemVO {
    private String subjectCode;    // 科目编码
    private String subjectName;     // 科目名称
    private BigDecimal balance;    // 余额
    private String category;        // 分类（流动资产/非流动资产/流动负债/非流动负债/所有者权益）
}





