package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.mapper.AccountingSubjectMapper;
import com.example.demo.mapper.JournalEntryMapper;
import com.example.demo.mapper.PurchaseOrderMapper;
import com.example.demo.mapper.SalesOrderMapper;
import com.example.demo.mapper.ProjectMapper;
import com.example.demo.model.entity.AccountingSubject;
import com.example.demo.model.entity.JournalEntry;
import com.example.demo.model.entity.Project;
import com.example.demo.model.entity.PurchaseOrder;
import com.example.demo.model.entity.SalesOrder;
import com.example.demo.model.vo.AccountBalanceVO;
import com.example.demo.model.vo.BalanceSheetItemVO;
import com.example.demo.model.vo.BalanceSheetVO;
import com.example.demo.model.vo.CashFlowItemVO;
import com.example.demo.model.vo.CashFlowVO;
import com.example.demo.model.vo.ProfitLossItemVO;
import com.example.demo.model.vo.ProfitLossVO;
import com.example.demo.model.vo.ProjectReportVO;
import com.example.demo.model.vo.ProjectOrderItemVO;
import com.example.demo.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 报表服务实现类
 */
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private JournalEntryMapper journalEntryMapper;

    @Autowired
    private AccountingSubjectMapper accountingSubjectMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private PurchaseOrderMapper purchaseOrderMapper;

    @Autowired
    private SalesOrderMapper salesOrderMapper;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public List<AccountBalanceVO> generateAccountBalance(String startDate, String endDate) {
        List<AccountBalanceVO> result = new ArrayList<>();
        
        try {
            Date start = DATE_FORMAT.parse(startDate);
            Date end = DATE_FORMAT.parse(endDate);
            // 结束日期设置为当天的23:59:59
            Calendar cal = Calendar.getInstance();
            cal.setTime(end);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            final Date endDateFinal = cal.getTime(); // 使用final变量供lambda使用

            // 查询所有启用的科目
            LambdaQueryWrapper<AccountingSubject> subjectWrapper = new LambdaQueryWrapper<>();
            subjectWrapper.eq(AccountingSubject::getStatus, "启用");
            List<AccountingSubject> subjects = accountingSubjectMapper.selectList(subjectWrapper);

            // 查询指定期间内的所有已过账分录（报表只统计已过账的分录）
            // 注意：优先使用post_time（过账时间），如果post_time为空则使用create_time
            LambdaQueryWrapper<JournalEntry> entryWrapper = new LambdaQueryWrapper<>();
            entryWrapper.eq(JournalEntry::getStatus, "已过账");
            // 使用过账时间过滤，如果post_time为空，则使用create_time作为备选
            final Date startFinal = start; // 使用final变量供lambda使用
            entryWrapper.and(wrapper -> {
                wrapper.and(w -> w.isNotNull(JournalEntry::getPost_time)
                        .ge(JournalEntry::getPost_time, startFinal)
                        .le(JournalEntry::getPost_time, endDateFinal))
                .or(w -> w.isNull(JournalEntry::getPost_time)
                        .ge(JournalEntry::getCreate_time, startFinal)
                        .le(JournalEntry::getCreate_time, endDateFinal));
            });
            List<JournalEntry> entries = journalEntryMapper.selectList(entryWrapper);
            
            // 调试日志
            System.out.println("🔍 [科目余额表] 查询参数: startDate=" + startDate + ", endDate=" + endDate);
            System.out.println("🔍 [科目余额表] 查询到的分录数量: " + entries.size());
            if (!entries.isEmpty()) {
                JournalEntry first = entries.get(0);
                System.out.println("🔍 [科目余额表] 第一条分录: ID=" + first.getId() + ", Status=" + first.getStatus() + ", PostTime=" + first.getPost_time() + ", CreateTime=" + first.getCreate_time());
            } else {
                // 如果没有查询到数据，检查是否有已过账的分录
                LambdaQueryWrapper<JournalEntry> allPostedWrapper = new LambdaQueryWrapper<>();
                allPostedWrapper.eq(JournalEntry::getStatus, "已过账");
                long allPostedCount = journalEntryMapper.selectCount(allPostedWrapper);
                System.out.println("🔍 [科目余额表] 数据库中已过账分录总数: " + allPostedCount);
                
                // 检查日期范围外的已过账分录
                LambdaQueryWrapper<JournalEntry> outsideRangeWrapper = new LambdaQueryWrapper<>();
                outsideRangeWrapper.eq(JournalEntry::getStatus, "已过账");
                List<JournalEntry> outsideEntries = journalEntryMapper.selectList(outsideRangeWrapper);
                if (!outsideEntries.isEmpty()) {
                    JournalEntry sample = outsideEntries.get(0);
                    System.out.println("🔍 [科目余额表] 日期范围外的已过账分录示例: ID=" + sample.getId() + ", PostTime=" + sample.getPost_time() + ", CreateTime=" + sample.getCreate_time());
                }
            }

            // 按科目编码分组统计
            Map<String, AccountBalanceVO> balanceMap = new HashMap<>();
            
            // 初始化所有科目
            for (AccountingSubject subject : subjects) {
                AccountBalanceVO vo = new AccountBalanceVO();
                vo.setSubjectCode(subject.getSubjectCode());
                vo.setSubjectName(subject.getSubjectName());
                vo.setSubjectType(subject.getSubjectType());
                vo.setBeginBalance(BigDecimal.ZERO);
                vo.setDebitAmount(BigDecimal.ZERO);
                vo.setCreditAmount(BigDecimal.ZERO);
                vo.setEndBalance(BigDecimal.ZERO);
                balanceMap.put(subject.getSubjectCode(), vo);
            }

            // 统计本期发生额
            for (JournalEntry entry : entries) {
                // 借方科目
                String debitCode = entry.getDebit_code();
                AccountBalanceVO debitVO = balanceMap.get(debitCode);
                if (debitVO != null) {
                    BigDecimal amount = entry.getAmount() != null ? entry.getAmount() : BigDecimal.ZERO;
                    debitVO.setDebitAmount(debitVO.getDebitAmount().add(amount));
                }

                // 贷方科目
                String creditCode = entry.getCredit_code();
                AccountBalanceVO creditVO = balanceMap.get(creditCode);
                if (creditVO != null) {
                    BigDecimal amount = entry.getAmount() != null ? entry.getAmount() : BigDecimal.ZERO;
                    creditVO.setCreditAmount(creditVO.getCreditAmount().add(amount));
                }
            }

            // 计算期末余额（简化处理：资产类借方增加，负债/权益类贷方增加）
            for (AccountBalanceVO vo : balanceMap.values()) {
                BigDecimal debit = vo.getDebitAmount();
                BigDecimal credit = vo.getCreditAmount();
                
                String type = vo.getSubjectType();
                if ("资产".equals(type) || "成本".equals(type)) {
                    // 资产类和成本类：期末余额 = 期初余额 + 借方 - 贷方
                    vo.setEndBalance(vo.getBeginBalance().add(debit).subtract(credit));
                } else if ("负债".equals(type) || "所有者权益".equals(type) || "损益".equals(type)) {
                    // 负债类、权益类和损益类：期末余额 = 期初余额 + 贷方 - 借方
                    vo.setEndBalance(vo.getBeginBalance().add(credit).subtract(debit));
                }
            }

            // 过滤掉没有发生额的科目（可选，根据需求决定）
            result = balanceMap.values().stream()
                    .filter(vo -> vo.getDebitAmount().compareTo(BigDecimal.ZERO) > 0 
                            || vo.getCreditAmount().compareTo(BigDecimal.ZERO) > 0
                            || vo.getBeginBalance().compareTo(BigDecimal.ZERO) > 0)
                    .sorted(Comparator.comparing(AccountBalanceVO::getSubjectCode))
                    .collect(Collectors.toList());

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public ProfitLossVO generateProfitLoss(String startDate, String endDate) {
        ProfitLossVO result = new ProfitLossVO();
        result.setPeriod(startDate + " 至 " + endDate);
        result.setRevenue(BigDecimal.ZERO);
        result.setCostOfSales(BigDecimal.ZERO);
        result.setGrossProfit(BigDecimal.ZERO);
        result.setOperatingExpenses(BigDecimal.ZERO);
        result.setOperatingProfit(BigDecimal.ZERO);
        result.setNetProfit(BigDecimal.ZERO);
        result.setItems(new ArrayList<>());

        try {
            Date start = DATE_FORMAT.parse(startDate);
            Date end = DATE_FORMAT.parse(endDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(end);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            end = cal.getTime();

            // 查询指定期间内的所有已过账分录（报表只统计已过账的分录）
            LambdaQueryWrapper<JournalEntry> entryWrapper = new LambdaQueryWrapper<>();
            entryWrapper.eq(JournalEntry::getStatus, "已过账");
            entryWrapper.ge(JournalEntry::getCreate_time, start);
            entryWrapper.le(JournalEntry::getCreate_time, end);
            List<JournalEntry> entries = journalEntryMapper.selectList(entryWrapper);

            // 查询损益类科目（注意这里使用的是 Java 字段名 subjectType，而不是数据库字段 subject_type）
            LambdaQueryWrapper<AccountingSubject> subjectWrapper = new LambdaQueryWrapper<>();
            subjectWrapper.eq(AccountingSubject::getStatus, "启用");
            subjectWrapper.eq(AccountingSubject::getSubjectType, "损益");
            List<AccountingSubject> profitLossSubjects = accountingSubjectMapper.selectList(subjectWrapper);

            // 按科目统计收入、成本、费用
            Map<String, BigDecimal> subjectAmountMap = new HashMap<>();
            
            for (JournalEntry entry : entries) {
                // 统计贷方科目（收入类）
                String creditCode = entry.getCredit_code();
                AccountingSubject creditSubject = profitLossSubjects.stream()
                        .filter(s -> s.getSubjectCode().equals(creditCode))
                        .findFirst()
                        .orElse(null);
                
                if (creditSubject != null) {
                    BigDecimal amount = entry.getAmount() != null ? entry.getAmount() : BigDecimal.ZERO;
                    subjectAmountMap.put(creditCode, 
                            subjectAmountMap.getOrDefault(creditCode, BigDecimal.ZERO).add(amount));
                }

                // 统计借方科目（成本、费用类）
                String debitCode = entry.getDebit_code();
                AccountingSubject debitSubject = profitLossSubjects.stream()
                        .filter(s -> s.getSubjectCode().equals(debitCode))
                        .findFirst()
                        .orElse(null);
                
                if (debitSubject != null) {
                    BigDecimal amount = entry.getAmount() != null ? entry.getAmount() : BigDecimal.ZERO;
                    subjectAmountMap.put(debitCode, 
                            subjectAmountMap.getOrDefault(debitCode, BigDecimal.ZERO).add(amount));
                }
            }

            // 构建明细项并分类
            BigDecimal revenue = BigDecimal.ZERO;
            BigDecimal costOfSales = BigDecimal.ZERO;
            BigDecimal operatingExpenses = BigDecimal.ZERO;

            for (AccountingSubject subject : profitLossSubjects) {
                BigDecimal amount = subjectAmountMap.getOrDefault(subject.getSubjectCode(), BigDecimal.ZERO);
                if (amount.compareTo(BigDecimal.ZERO) > 0) {
                    ProfitLossItemVO item = new ProfitLossItemVO();
                    item.setSubjectCode(subject.getSubjectCode());
                    item.setSubjectName(subject.getSubjectName());
                    item.setAmount(amount);

                    // 根据科目编码判断类型（注意：要先判断更具体的，再判断通用的）
                    String code = subject.getSubjectCode();
                    if (code.startsWith("64")) {
                        // 64开头是成本类（6401主营业务成本、6402其他业务成本）
                        item.setItemType("成本");
                        costOfSales = costOfSales.add(amount);
                    } else if (code.startsWith("66") || code.startsWith("67")) {
                        // 66、67开头是费用类（6601销售费用、6602管理费用、6603财务费用、6711营业外支出）
                        item.setItemType("费用");
                        operatingExpenses = operatingExpenses.add(amount);
                    } else if (code.startsWith("6")) {
                        // 6开头是收入类（6001主营业务收入、6051其他业务收入、6111资产处置收益、6301营业外收入）
                        // 注意：这个判断要放在64、66、67之后，避免误判
                        item.setItemType("收入");
                        revenue = revenue.add(amount);
                    } else {
                        // 其他损益类作为费用（兜底处理）
                        item.setItemType("费用");
                        operatingExpenses = operatingExpenses.add(amount);
                    }
                    result.getItems().add(item);
                }
            }

            result.setRevenue(revenue);
            result.setCostOfSales(costOfSales);
            result.setGrossProfit(revenue.subtract(costOfSales));
            result.setOperatingExpenses(operatingExpenses);
            result.setOperatingProfit(revenue.subtract(costOfSales).subtract(operatingExpenses));
            result.setNetProfit(result.getOperatingProfit()); // 简化处理，净利润=营业利润

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public BalanceSheetVO generateBalanceSheet(String reportDate) {
        BalanceSheetVO result = new BalanceSheetVO();
        result.setReportDate(reportDate);
        result.setAssets(new ArrayList<>());
        result.setLiabilities(new ArrayList<>());
        result.setEquity(new ArrayList<>());
        result.setTotalAssets(BigDecimal.ZERO);
        result.setTotalLiabilities(BigDecimal.ZERO);
        result.setTotalEquity(BigDecimal.ZERO);

        try {
            Date endDate = DATE_FORMAT.parse(reportDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(endDate);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            endDate = cal.getTime();

            // 查询所有启用的科目
            LambdaQueryWrapper<AccountingSubject> subjectWrapper = new LambdaQueryWrapper<>();
            subjectWrapper.eq(AccountingSubject::getStatus, "启用");
            List<AccountingSubject> subjects = accountingSubjectMapper.selectList(subjectWrapper);

            // 查询报表日期之前的所有已过账分录（报表只统计已过账的分录）
            LambdaQueryWrapper<JournalEntry> entryWrapper = new LambdaQueryWrapper<>();
            entryWrapper.eq(JournalEntry::getStatus, "已过账");
            entryWrapper.le(JournalEntry::getCreate_time, endDate);
            List<JournalEntry> entries = journalEntryMapper.selectList(entryWrapper);

            // 按科目编码统计余额
            Map<String, BigDecimal> subjectBalanceMap = new HashMap<>();

            for (JournalEntry entry : entries) {
                BigDecimal amount = entry.getAmount() != null ? entry.getAmount() : BigDecimal.ZERO;
                
                // 借方科目
                String debitCode = entry.getDebit_code();
                subjectBalanceMap.put(debitCode, 
                        subjectBalanceMap.getOrDefault(debitCode, BigDecimal.ZERO).add(amount));

                // 贷方科目
                String creditCode = entry.getCredit_code();
                subjectBalanceMap.put(creditCode, 
                        subjectBalanceMap.getOrDefault(creditCode, BigDecimal.ZERO).subtract(amount));
            }

            // 按科目类型分类
            BigDecimal totalAssets = BigDecimal.ZERO;
            BigDecimal totalLiabilities = BigDecimal.ZERO;
            BigDecimal totalEquity = BigDecimal.ZERO;

            for (AccountingSubject subject : subjects) {
                String code = subject.getSubjectCode();
                BigDecimal balance = subjectBalanceMap.getOrDefault(code, BigDecimal.ZERO);
                
                if (balance.compareTo(BigDecimal.ZERO) == 0) {
                    continue;
                }

                BalanceSheetItemVO item = new BalanceSheetItemVO();
                item.setSubjectCode(code);
                item.setSubjectName(subject.getSubjectName());
                
                String type = subject.getSubjectType();
                if ("资产".equals(type)) {
                    // 资产类：余额为正表示资产
                    if (balance.compareTo(BigDecimal.ZERO) > 0) {
                        item.setBalance(balance);
                        item.setCategory(code.startsWith("1") ? "流动资产" : "非流动资产");
                        result.getAssets().add(item);
                        totalAssets = totalAssets.add(balance);
                    }
                } else if ("负债".equals(type)) {
                    // 负债类：余额为负表示负债（需要转为正数）
                    if (balance.compareTo(BigDecimal.ZERO) < 0) {
                        item.setBalance(balance.abs());
                        item.setCategory(code.startsWith("2") ? "流动负债" : "非流动负债");
                        result.getLiabilities().add(item);
                        totalLiabilities = totalLiabilities.add(balance.abs());
                    }
                } else if ("所有者权益".equals(type)) {
                    // 所有者权益类：余额为负表示权益（需要转为正数）
                    if (balance.compareTo(BigDecimal.ZERO) < 0) {
                        item.setBalance(balance.abs());
                        item.setCategory("所有者权益");
                        result.getEquity().add(item);
                        totalEquity = totalEquity.add(balance.abs());
                    }
                }
            }

            result.setTotalAssets(totalAssets);
            result.setTotalLiabilities(totalLiabilities);
            result.setTotalEquity(totalEquity);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 生成现金流量表（简化版）
     */
    @Override
    public CashFlowVO generateCashFlow(String startDate, String endDate) {
        CashFlowVO result = new CashFlowVO();
        result.setPeriod(startDate + " 至 " + endDate);

        try {
            Date start = DATE_FORMAT.parse(startDate);
            Date end = DATE_FORMAT.parse(endDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(end);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            end = cal.getTime();

            // 现金科目编码（可按需扩展）
            Set<String> cashCodes = new HashSet<>(Arrays.asList("1001", "1002"));

            // 查询指定期间内的所有已过账分录（报表只统计已过账的分录）
            LambdaQueryWrapper<JournalEntry> entryWrapper = new LambdaQueryWrapper<>();
            entryWrapper.eq(JournalEntry::getStatus, "已过账");
            entryWrapper.ge(JournalEntry::getCreate_time, start);
            entryWrapper.le(JournalEntry::getCreate_time, end);
            List<JournalEntry> entries = journalEntryMapper.selectList(entryWrapper);

            Map<String, CashFlowItemVO> itemMap = new LinkedHashMap<>();
            BigDecimal netOperating = BigDecimal.ZERO;
            BigDecimal netInvesting = BigDecimal.ZERO;
            BigDecimal netFinancing = BigDecimal.ZERO;

            for (JournalEntry entry : entries) {
                String debitCode = entry.getDebit_code();
                String creditCode = entry.getCredit_code();
                BigDecimal amount = entry.getAmount() != null ? entry.getAmount() : BigDecimal.ZERO;
                if (amount.compareTo(BigDecimal.ZERO) == 0) {
                    continue;
                }

                boolean debitIsCash = cashCodes.contains(debitCode);
                boolean creditIsCash = cashCodes.contains(creditCode);

                if (!debitIsCash && !creditIsCash) {
                    continue; // 与现金无关
                }

                // 现金在借方视为流入，在贷方视为流出（简化处理）
                BigDecimal cashFlow;
                if (debitIsCash && !creditIsCash) {
                    cashFlow = amount; // 流入
                } else if (creditIsCash && !debitIsCash) {
                    cashFlow = amount.negate(); // 流出
                } else {
                    // 两边都是现金，视为内部划转
                    continue;
                }

                String sourceType = entry.getSource_type() != null ? entry.getSource_type().toUpperCase() : "";
                String category = "经营活动";
                String itemName = "其他经营活动现金收支";

                if ("SALES".equals(sourceType)) {
                    category = "经营活动";
                    itemName = cashFlow.compareTo(BigDecimal.ZERO) >= 0 ? "销售收现" : "销售相关现金支出";
                } else if ("PURCHASE".equals(sourceType)) {
                    category = "经营活动";
                    itemName = cashFlow.compareTo(BigDecimal.ZERO) >= 0 ? "采购退现" : "采购付现";
                } else if ("PAYMENT".equalsIgnoreCase(sourceType)) {
                    category = "经营活动";
                    itemName = "向供应商付款";
                } else if ("RECEIPT".equalsIgnoreCase(sourceType)) {
                    category = "经营活动";
                    itemName = "向客户收款";
                } else if ("MANUAL".equals(sourceType)) {
                    category = "经营活动";
                    itemName = "手工记账产生的现金收支";
                }

                CashFlowItemVO item = itemMap.get(itemName);
                if (item == null) {
                    item = new CashFlowItemVO();
                    item.setItemName(itemName);
                    item.setCategory(category);
                    item.setAmount(BigDecimal.ZERO);
                    itemMap.put(itemName, item);
                }
                item.setAmount(item.getAmount().add(cashFlow));

                if ("经营活动".equals(category)) {
                    netOperating = netOperating.add(cashFlow);
                } else if ("投资活动".equals(category)) {
                    netInvesting = netInvesting.add(cashFlow);
                } else if ("筹资活动".equals(category)) {
                    netFinancing = netFinancing.add(cashFlow);
                }
            }

            result.setItems(new ArrayList<>(itemMap.values()));
            result.setNetOperating(netOperating);
            result.setNetInvesting(netInvesting);
            result.setNetFinancing(netFinancing);
            result.setNetIncrease(netOperating.add(netInvesting).add(netFinancing));

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public List<ProjectReportVO> generateProjectReport(Long projectId, String startDate, String endDate) {
        List<ProjectReportVO> result = new ArrayList<>();
        
        try {
            // 1. 查询项目列表
            List<Project> projects;
            if (projectId != null) {
                // 查询指定项目
                Project project = projectMapper.selectById(projectId);
                if (project != null) {
                    projects = Collections.singletonList(project);
                } else {
                    return result; // 项目不存在，返回空列表
                }
            } else {
                // 查询所有项目（使用自定义分页查询方法，确保字段映射正确）
                Page<Project> page = new Page<>(1, 10000); // 设置一个大的pageSize来获取所有数据
                IPage<Project> pageResult = projectMapper.selectProjectPage(page, null, null, null);
                projects = pageResult.getRecords();
            }
            
            // 2. 处理日期范围（如果提供）
            Date start = null;
            Date end = null;
            if (startDate != null && !startDate.isEmpty()) {
                start = DATE_FORMAT.parse(startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                end = DATE_FORMAT.parse(endDate);
                Calendar cal = Calendar.getInstance();
                cal.setTime(end);
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                end = cal.getTime();
            }
            
            // 3. 为每个项目生成报表数据
            for (Project project : projects) {
                ProjectReportVO report = new ProjectReportVO();
                
                // 设置项目基本信息
                report.setProjectId(project.getProjectId());
                report.setProjectCode(project.getProjectCode());
                report.setProjectName(project.getProjectName());
                report.setProjectManager(project.getProjectManager());
                report.setStartDate(project.getStartDate());
                report.setEndDate(project.getEndDate());
                report.setBudget(project.getBudget() != null ? project.getBudget() : BigDecimal.ZERO);
                report.setStatus(project.getStatus());
                
                // 4. 查询关联的采购订单
                QueryWrapper<PurchaseOrder> purchaseWrapper = new QueryWrapper<>();
                purchaseWrapper.eq("project_id", project.getProjectId());
                if (start != null) {
                    purchaseWrapper.ge("create_time", start);
                }
                if (end != null) {
                    purchaseWrapper.le("create_time", end);
                }
                List<PurchaseOrder> purchaseOrders = purchaseOrderMapper.selectList(purchaseWrapper);
                
                // 统计采购订单
                int purchaseCount = purchaseOrders.size();
                BigDecimal purchaseAmount = BigDecimal.ZERO;
                List<ProjectOrderItemVO> purchaseOrderItems = new ArrayList<>();
                for (PurchaseOrder order : purchaseOrders) {
                    if (order.getTotal_amount() != null) {
                        purchaseAmount = purchaseAmount.add(order.getTotal_amount());
                    }
                    
                    ProjectOrderItemVO item = new ProjectOrderItemVO();
                    item.setOrderId(order.getId());
                    item.setOrderNo(order.getOrder_no());
                    item.setOrderType("采购订单");
                    item.setAmount(order.getTotal_amount());
                    item.setStatus(order.getStatus());
                    item.setCreateTime(order.getCreate_time());
                    item.setSupplierName(order.getSupplier_name());
                    purchaseOrderItems.add(item);
                }
                report.setPurchaseOrderCount(purchaseCount);
                report.setPurchaseAmount(purchaseAmount);
                report.setPurchaseOrders(purchaseOrderItems);
                
                // 5. 查询关联的销售订单
                QueryWrapper<SalesOrder> salesWrapper = new QueryWrapper<>();
                salesWrapper.eq("project_id", project.getProjectId());
                if (start != null) {
                    salesWrapper.ge("create_time", start);
                }
                if (end != null) {
                    salesWrapper.le("create_time", end);
                }
                List<SalesOrder> salesOrders = salesOrderMapper.selectList(salesWrapper);
                
                // 统计销售订单
                int salesCount = salesOrders.size();
                BigDecimal salesAmount = BigDecimal.ZERO;
                List<ProjectOrderItemVO> salesOrderItems = new ArrayList<>();
                for (SalesOrder order : salesOrders) {
                    if (order.getTotal_amount() != null) {
                        salesAmount = salesAmount.add(order.getTotal_amount());
                    }
                    
                    ProjectOrderItemVO item = new ProjectOrderItemVO();
                    item.setOrderId(order.getId());
                    item.setOrderNo(order.getOrder_no());
                    item.setOrderType("销售订单");
                    item.setAmount(order.getTotal_amount());
                    item.setStatus(order.getStatus());
                    item.setCreateTime(order.getCreate_time());
                    item.setCustomerName(order.getCustomer_name());
                    salesOrderItems.add(item);
                }
                report.setSalesOrderCount(salesCount);
                report.setSalesAmount(salesAmount);
                report.setSalesOrders(salesOrderItems);
                
                // 6. 计算收支情况
                report.setTotalIncome(salesAmount);
                report.setTotalExpense(purchaseAmount);
                report.setNetProfit(salesAmount.subtract(purchaseAmount));
                
                // 7. 计算预算使用率
                if (report.getBudget() != null && report.getBudget().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal utilization = purchaseAmount.divide(report.getBudget(), 4, java.math.RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"));
                    report.setBudgetUtilization(utilization);
                } else {
                    report.setBudgetUtilization(BigDecimal.ZERO);
                }
                
                result.add(report);
            }
            
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return result;
    }
}





