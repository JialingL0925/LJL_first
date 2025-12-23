package com.example.demo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.model.entity.JournalEntry;
import com.example.demo.model.vo.ResultVO;
import com.example.demo.service.JournalEntryService;
import com.example.demo.util.ResultUtil;
import com.example.demo.model.entity.Employee;
import com.example.demo.mapper.EmployeeMapper;
import com.example.demo.mapper.JournalEntryMapper;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 分录控制器
 * 接口前缀: /accounting/journalEntry
 */
@Slf4j
@RestController
@RequestMapping("/accounting/journalEntry")
public class JournalEntryController {

    @Resource
    private JournalEntryService journalEntryService;
    
    @Resource
    private EmployeeMapper employeeMapper;
    
    @Resource
    private JournalEntryMapper journalEntryMapper;
    
    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                String phone = userDetails.getUsername(); // 手机号作为用户名
                Employee employee = employeeMapper.selectByPhone(phone);
                if (employee != null) {
                    return employee.getEmployeeId();
                }
            }
        } catch (Exception e) {
            log.error("获取当前用户ID失败", e);
        }
        return null;
    }

    /**
     * 新增分录
     */
    @PostMapping("/add")
    public ResultVO addJournalEntry(@RequestBody JournalEntry journalEntry) {
        log.info("新增分录请求参数: {}", journalEntry);
        try {
            boolean save = journalEntryService.saveManualEntry(journalEntry);
            if (save) {
                log.info("新增分录成功, ID: {}", journalEntry.getId());
                return ResultUtil.success("新增分录成功", journalEntry.getId());
            } else {
                log.warn("新增分录失败, 参数: {}", journalEntry);
                return ResultUtil.systemError("新增分录失败");
            }
        } catch (Exception e) {
            log.error("新增分录异常", e);
            return ResultUtil.systemError("新增分录失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询分录列表
     */
    @GetMapping("/list")
    public ResultVO getJournalEntryList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String entryNo,
            @RequestParam(required = false) String sourceType,
            @RequestParam(required = false) Long sourceId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String entryDateStart,
            @RequestParam(required = false) String entryDateEnd) {
        log.info("查询分录列表参数: pageNum={}, pageSize={}, entryNo={}, sourceType={}, sourceId={}, status={}, entryDateStart={}, entryDateEnd={}",
                pageNum, pageSize, entryNo, sourceType, sourceId, status, entryDateStart, entryDateEnd);
        try {
            Page<JournalEntry> page = new Page<>(pageNum, pageSize);
            IPage<JournalEntry> pageResult = journalEntryService.pageList(page, entryNo, sourceType, sourceId, status, entryDateStart, entryDateEnd);
            log.info("查询分录列表成功, 总数: {}", pageResult.getTotal());
            
            // 详细日志：打印返回的数据状态
            if (pageResult != null && pageResult.getRecords() != null && !pageResult.getRecords().isEmpty()) {
                JournalEntry firstEntry = pageResult.getRecords().get(0);
                log.info("🔍 [Controller] 返回的第一条数据 - ID: {}, Status: {}", firstEntry.getId(), firstEntry.getStatus());
                log.info("🔍 [Controller] 返回的第一条数据完整对象: {}", firstEntry);
            }
            
            return ResultUtil.success("查询成功", pageResult);
        } catch (Exception e) {
            log.error("查询分录列表异常", e);
            return ResultUtil.systemError("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询分录详情
     */
    @GetMapping("/detail/{id}")
    public ResultVO getJournalEntryDetail(@PathVariable Long id) {
        log.info("查询分录详情, ID: {}", id);
        try {
            JournalEntry journalEntry = journalEntryService.getById(id);
            if (journalEntry != null) {
                // 详细日志：打印查询到的原始数据
                log.info("🔍 [Controller] 查询详情 - ID: {}, Status: '{}'", journalEntry.getId(), journalEntry.getStatus());
                log.info("🔍 [Controller] 查询详情 - 完整对象: {}", journalEntry);
                log.info("🔍 [Controller] 查询详情 - Status字段类型: {}, 值: '{}', 长度: {}", 
                    journalEntry.getStatus() != null ? journalEntry.getStatus().getClass().getName() : "null",
                    journalEntry.getStatus(),
                    journalEntry.getStatus() != null ? journalEntry.getStatus().length() : 0);
                return ResultUtil.success("查询成功", journalEntry);
            } else {
                log.warn("分录不存在, ID: {}", id);
                return ResultUtil.systemError("查询失败: 分录不存在");
            }
        } catch (Exception e) {
            log.error("查询分录详情异常, ID: {}", id, e);
            return ResultUtil.systemError("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据来源类型和ID查询分录
     */
    @GetMapping("/bySource")
    public ResultVO getJournalEntryBySource(
            @RequestParam String sourceType,
            @RequestParam Long sourceId) {
        log.info("根据来源查询分录, 来源类型: {}, 来源ID: {}", sourceType, sourceId);
        try {
            if (sourceType == null || sourceType.isEmpty()) {
                return ResultUtil.systemError("查询失败: 来源类型不能为空");
            }
            if (sourceId == null) {
                return ResultUtil.systemError("查询失败: 来源ID不能为空");
            }
            // 统一转换为大写以匹配数据库存储格式
            sourceType = sourceType.toUpperCase();
            // 根据前端传递的sourceType映射到后端存储的值
            if ("PURCHASE_ORDER".equals(sourceType)) {
                sourceType = "PURCHASE";
            }

            List<JournalEntry> journalEntries = journalEntryService.getBySource(sourceType, sourceId);
            return ResultUtil.success("查询成功", journalEntries);
        } catch (Exception e) {
            log.error("根据来源查询分录异常, 来源类型: {}, 来源ID: {}", sourceType, sourceId, e);
            return ResultUtil.systemError("查询失败: " + e.getMessage());
        }
    }

    /**
     * 修改分录信息
     */
    @PutMapping("/update")
    public ResultVO updateJournalEntry(@RequestBody JournalEntry journalEntry) {
        log.info("修改分录请求参数: {}", journalEntry);
        try {
            if (journalEntry.getId() == null) {
                return ResultUtil.systemError("修改失败: 分录ID不能为空");
            }
            boolean update = journalEntryService.updateById(journalEntry);
            if (update) {
                log.info("修改分录成功, ID: {}", journalEntry.getId());
                return ResultUtil.success("修改分录成功", null);
            } else {
                log.warn("修改分录失败, ID: {} (可能不存在或未变更)", journalEntry.getId());
                return ResultUtil.systemError("修改分录失败: 未找到该分录或信息未变更");
            }
        } catch (Exception e) {
            log.error("修改分录异常", e);
            return ResultUtil.systemError("修改失败: " + e.getMessage());
        }
    }

    /**
     * 删除分录
     */
    @DeleteMapping("/delete/{id}")
    public ResultVO deleteJournalEntry(@PathVariable Long id) {
        log.info("删除分录请求, ID: {}", id);
        try {
            boolean remove = journalEntryService.removeById(id);
            if (remove) {
                log.info("删除分录成功, ID: {}", id);
                return ResultUtil.success("删除分录成功", null);
            } else {
                log.warn("删除分录失败, ID: {} (可能不存在)", id);
                return ResultUtil.systemError("删除分录失败: 未找到该分录");
            }
        } catch (Exception e) {
            log.error("删除分录异常, ID: {}", id, e);
            return ResultUtil.systemError("删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量新增分录
     */
    @PostMapping("/batchAdd")
    public ResultVO batchAddJournalEntry(@RequestBody List<JournalEntry> journalEntries) {
        log.info("批量新增分录请求参数数量: {}", journalEntries.size());
        try {
            boolean save = journalEntryService.saveBatch(journalEntries);
            if (save) {
                log.info("批量新增分录成功, 数量: {}", journalEntries.size());
                return ResultUtil.success("批量新增分录成功", null);
            } else {
                log.warn("批量新增分录失败");
                return ResultUtil.systemError("批量新增分录失败");
            }
        } catch (Exception e) {
            log.error("批量新增分录异常", e);
            return ResultUtil.systemError("批量新增分录失败: " + e.getMessage());
        }
    }

    /**
     * 切换分录状态
     */
    @PutMapping("/changeStatus")
    public ResultVO changeStatus(
            @RequestParam Long id,
            @RequestParam Integer status) {
        log.info("切换分录状态请求: ID={}, 目标状态={}", id, status);
        try {
            if (id == null) {
                return ResultUtil.systemError("切换失败: 分录ID不能为空");
            }
            if (status == null) {
                return ResultUtil.systemError("切换失败: 状态值不能为空");
            }

            JournalEntry exist = journalEntryService.getById(id);
            if (exist == null) {
                return ResultUtil.systemError("切换失败: 分录不存在");
            }

            JournalEntry journalEntry = new JournalEntry();
            journalEntry.setId(id);
            journalEntry.setStatus(status.toString());
            boolean update = journalEntryService.updateById(journalEntry);

            if (update) {
                String msg = "切换分录状态成功";
                return ResultUtil.success(msg, null);
            } else {
                return ResultUtil.systemError("切换失败: 状态未变更 (可能已为目标状态)");
            }
        } catch (Exception e) {
            log.error("切换分录状态异常, ID: {}", id, e);
            return ResultUtil.systemError("切换失败: " + e.getMessage());
        }
    }

    /**
     * 过账单个分录
     */
    @PostMapping("/post")
    public ResultVO postEntry(@RequestParam Long id) {
        log.info("过账分录请求, ID: {}", id);
        try {
            if (id == null) {
                return ResultUtil.systemError("过账失败: 分录ID不能为空");
            }

            Long userId = getCurrentUserId();
            if (userId == null) {
                return ResultUtil.systemError("过账失败: 无法获取当前用户信息");
            }

            boolean success = journalEntryService.postEntry(id, userId);
            if (success) {
                log.info("过账分录成功, ID: {}", id);
                return ResultUtil.success("过账成功", null);
            } else {
                return ResultUtil.systemError("过账失败");
            }
        } catch (Exception e) {
            log.error("过账分录异常, ID: {}", id, e);
            return ResultUtil.systemError("过账失败: " + e.getMessage());
        }
    }

    /**
     * 批量过账分录
     */
    @PostMapping("/batchPost")
    public ResultVO batchPostEntry(@RequestBody List<Long> ids) {
        log.info("批量过账分录请求, 数量: {}", ids != null ? ids.size() : 0);
        try {
            if (ids == null || ids.isEmpty()) {
                return ResultUtil.systemError("过账失败: 分录ID列表不能为空");
            }

            Long userId = getCurrentUserId();
            if (userId == null) {
                return ResultUtil.systemError("过账失败: 无法获取当前用户信息");
            }

            boolean success = journalEntryService.batchPostEntry(ids, userId);
            if (success) {
                log.info("批量过账分录成功, 数量: {}", ids.size());
                return ResultUtil.success("批量过账成功", null);
            } else {
                return ResultUtil.systemError("批量过账失败");
            }
        } catch (Exception e) {
            log.error("批量过账分录异常", e);
            return ResultUtil.systemError("批量过账失败: " + e.getMessage());
        }
    }

    /**
     * 测试接口：直接查询数据库原始数据（用于调试）
     */
    @GetMapping("/test/directQuery/{id}")
    public ResultVO testDirectQuery(@PathVariable Long id) {
        log.info("测试直接查询数据库, ID: {}", id);
        try {
            // 方法1：使用原生SQL直接查询，返回Map
            Map<String, Object> rawData = journalEntryMapper.selectRawById(id);
            log.info("🔍 [测试接口] 原生SQL查询结果: {}", rawData);
            
            // 方法2：使用MyBatis-Plus的getById方法
            JournalEntry entry = journalEntryService.getById(id);
            if (entry == null) {
                return ResultUtil.systemError("记录不存在");
            }
            
            // 构建详细调试信息
            java.util.Map<String, Object> debugInfo = new java.util.HashMap<>();
            debugInfo.put("rawDataFromDB", rawData); // 原生SQL查询的原始数据
            debugInfo.put("mybatisPlusEntry", entry); // MyBatis-Plus查询的结果
            
            // 对比原始数据和MyBatis-Plus查询结果
            if (rawData != null) {
                Object rawStatus = rawData.get("status");
                debugInfo.put("rawStatus", rawStatus);
                debugInfo.put("rawStatusClass", rawStatus != null ? rawStatus.getClass().getName() : "null");
                debugInfo.put("rawStatusString", rawStatus != null ? rawStatus.toString() : "null");
                
                // 对比
                boolean statusMatch = entry.getStatus() != null && entry.getStatus().equals(rawStatus);
                debugInfo.put("statusMatch", statusMatch);
                
                if (!statusMatch) {
                    log.warn("⚠️ [测试接口] 状态不匹配！原始数据: '{}', MyBatis-Plus: '{}'", rawStatus, entry.getStatus());
                }
            }
            
            debugInfo.put("mybatisPlusStatus", entry.getStatus());
            debugInfo.put("mybatisPlusStatusClass", entry.getStatus() != null ? entry.getStatus().getClass().getName() : "null");
            debugInfo.put("mybatisPlusStatusLength", entry.getStatus() != null ? entry.getStatus().length() : 0);
            
            // 检查status是否等于"已过账"
            boolean isPosted = "已过账".equals(entry.getStatus());
            debugInfo.put("isPosted", isPosted);
            
            log.info("🔍 [测试接口] 完整调试信息: {}", debugInfo);
            
            return ResultUtil.success("查询成功", debugInfo);
        } catch (Exception e) {
            log.error("测试直接查询异常, ID: {}", id, e);
            return ResultUtil.systemError("查询失败: " + e.getMessage());
        }
    }
}