package com.example.demo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.model.entity.AccountingSubject;
import com.example.demo.service.AccountingSubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 会计科目控制器
 */
@RestController
@RequestMapping("/accounting/accountingSubject")
public class AccountingSubjectController {

    @Autowired
    private AccountingSubjectService accountingSubjectService;

    /**
     * 分页查询（修改：统一包装为含data字段的结构，适配前端）
     */
    @GetMapping("/page")
    public ResponseEntity<Map<String, Object>> pageList(  // 返回值改为Map
                                                          @RequestParam(defaultValue = "1") Long current,
                                                          @RequestParam(defaultValue = "10") Long size,
                                                          @RequestParam(required = false) String subjectName,
                                                          @RequestParam(required = false) String status,
                                                          @RequestParam(required = false) String subjectType) {
        Page<AccountingSubject> page = new Page<>(current, size);
        IPage<AccountingSubject> result = accountingSubjectService.pageList(page, subjectName, status, subjectType);

        // 包装分页数据到data字段，与其他接口格式统一
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", result);  // 分页数据放在data中
        return ResponseEntity.ok(response);
    }

    /**
     * 查询树形结构（修改：包装为统一格式）
     */
    @GetMapping("/tree")
    public ResponseEntity<Map<String, Object>> treeList(  // 返回值改为Map
                                                          @RequestParam(defaultValue = "0") Long parentSubjectId) {
        List<AccountingSubject> list = accountingSubjectService.treeList(parentSubjectId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", list);  // 树形数据放在data中
        return ResponseEntity.ok(response);
    }

    /**
     * 新增科目（保持不变，已符合格式）
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> save(@RequestBody AccountingSubject subject) {
        Map<String, Object> result = new HashMap<>();
        boolean success = accountingSubjectService.save(subject);
        result.put("success", success);
        result.put("message", success ? "新增成功" : "新增失败");
        return ResponseEntity.ok(result);
    }

    /**
     * 修改科目（保持不变，已符合格式）
     */
    @PutMapping
    public ResponseEntity<Map<String, Object>> update(@RequestBody AccountingSubject subject) {
        Map<String, Object> result = new HashMap<>();
        boolean success = accountingSubjectService.updateById(subject);
        result.put("success", success);
        result.put("message", success ? "修改成功" : "修改失败");
        return ResponseEntity.ok(result);
    }

    /**
     * 切换状态（仅修改接口路径，与前端调用匹配）
     */
    @PutMapping("/changeStatus")  // 路径从/status改为/changeStatus，适配前端API
    public ResponseEntity<Map<String, Object>> changeStatus(
            @RequestParam Long subjectId,
            @RequestParam String status,
            @RequestParam(required = false) String deactivationReason) {
        Map<String, Object> result = new HashMap<>();
        boolean success = accountingSubjectService.changeStatus(subjectId, status, deactivationReason);
        result.put("success", success);
        result.put("message", success ? "状态切换成功" : "状态切换失败");
        return ResponseEntity.ok(result);
    }

    /**
     * 根据ID查询（修改：包装为统一格式）
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {  // 返回值改为Map
        AccountingSubject subject = accountingSubjectService.getById(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", subject);  // 详情数据放在data中
        return ResponseEntity.ok(response);
    }
}