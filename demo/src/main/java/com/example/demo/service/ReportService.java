package com.example.demo.service;

import com.example.demo.model.vo.AccountBalanceVO;
import com.example.demo.model.vo.BalanceSheetVO;
import com.example.demo.model.vo.CashFlowVO;
import com.example.demo.model.vo.ProfitLossVO;
import com.example.demo.model.vo.ProjectReportVO;

import java.util.List;

/**
 * 报表服务接口
 */
public interface ReportService {
    /**
     * 生成科目余额表
     * @param startDate 开始日期（格式：yyyy-MM-dd）
     * @param endDate 结束日期（格式：yyyy-MM-dd）
     * @return 科目余额表列表
     */
    List<AccountBalanceVO> generateAccountBalance(String startDate, String endDate);

    /**
     * 生成利润表
     * @param startDate 开始日期（格式：yyyy-MM-dd）
     * @param endDate 结束日期（格式：yyyy-MM-dd）
     * @return 利润表数据
     */
    ProfitLossVO generateProfitLoss(String startDate, String endDate);

    /**
     * 生成资产负债表
     * @param reportDate 报表日期（格式：yyyy-MM-dd）
     * @return 资产负债表数据
     */
    BalanceSheetVO generateBalanceSheet(String reportDate);

    /**
     * 生成现金流量表（简化版）
     * @param startDate 开始日期（格式：yyyy-MM-dd）
     * @param endDate 结束日期（格式：yyyy-MM-dd）
     * @return 现金流量表数据
     */
    CashFlowVO generateCashFlow(String startDate, String endDate);

    /**
     * 生成项目报表
     * @param projectId 项目ID（可选，如果为null则查询所有项目）
     * @param startDate 开始日期（可选，格式：yyyy-MM-dd）
     * @param endDate 结束日期（可选，格式：yyyy-MM-dd）
     * @return 项目报表列表
     */
    List<ProjectReportVO> generateProjectReport(Long projectId, String startDate, String endDate);
}





