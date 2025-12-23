package com.example.demo.controller;

import com.example.demo.model.vo.AccountBalanceVO;
import com.example.demo.model.vo.BalanceSheetVO;
import com.example.demo.model.vo.CashFlowVO;
import com.example.demo.model.vo.ProfitLossVO;
import com.example.demo.model.vo.ProjectReportVO;
import com.example.demo.model.vo.ResultVO;
import com.example.demo.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 报表Controller
 */
@RestController
@RequestMapping("/accounting/report")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 生成科目余额表
     * @param startDate 开始日期（格式：yyyy-MM-dd）
     * @param endDate 结束日期（格式：yyyy-MM-dd）
     * @return 科目余额表数据
     */
    @GetMapping("/accountBalance")
    public ResultVO getAccountBalance(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            System.out.println("🔍 [报表Controller] 科目余额表查询请求: startDate=" + startDate + ", endDate=" + endDate);
            List<AccountBalanceVO> data = reportService.generateAccountBalance(startDate, endDate);
            System.out.println("🔍 [报表Controller] 返回数据条数: " + (data != null ? data.size() : 0));
            return ResultVO.success("查询成功", data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultVO.error(500, "生成科目余额表失败：" + e.getMessage());
        }
    }

    /**
     * 生成利润表
     * @param startDate 开始日期（格式：yyyy-MM-dd）
     * @param endDate 结束日期（格式：yyyy-MM-dd）
     * @return 利润表数据
     */
    @GetMapping("/profitLoss")
    public ResultVO getProfitLoss(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            ProfitLossVO data = reportService.generateProfitLoss(startDate, endDate);
            return ResultVO.success("查询成功", data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultVO.error(500, "生成利润表失败：" + e.getMessage());
        }
    }

    /**
     * 生成资产负债表
     * @param reportDate 报表日期（格式：yyyy-MM-dd）
     * @return 资产负债表数据
     */
    @GetMapping("/balanceSheet")
    public ResultVO getBalanceSheet(@RequestParam String reportDate) {
        try {
            BalanceSheetVO data = reportService.generateBalanceSheet(reportDate);
            return ResultVO.success("查询成功", data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultVO.error(500, "生成资产负债表失败：" + e.getMessage());
        }
    }

    /**
     * 生成现金流量表
     * @param startDate 开始日期（格式：yyyy-MM-dd）
     * @param endDate 结束日期（格式：yyyy-MM-dd）
     * @return 现金流量表数据
     */
    @GetMapping("/cashFlow")
    public ResultVO getCashFlow(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            CashFlowVO data = reportService.generateCashFlow(startDate, endDate);
            return ResultVO.success("查询成功", data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultVO.error(500, "生成现金流量表失败：" + e.getMessage());
        }
    }

    /**
     * 生成项目报表
     * @param projectId 项目ID（可选，如果为null则查询所有项目）
     * @param startDate 开始日期（可选，格式：yyyy-MM-dd）
     * @param endDate 结束日期（可选，格式：yyyy-MM-dd）
     * @return 项目报表列表
     */
    @GetMapping("/projectReport")
    public ResultVO getProjectReport(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            List<ProjectReportVO> data = reportService.generateProjectReport(projectId, startDate, endDate);
            return ResultVO.success("查询成功", data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultVO.error(500, "生成项目报表失败：" + e.getMessage());
        }
    }
}

