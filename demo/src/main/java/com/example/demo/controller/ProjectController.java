package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.model.entity.Project;
import com.example.demo.service.ProjectService;
import com.example.demo.model.vo.ResultVO;
import com.example.demo.util.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;

/**
 * 项目管理控制器
 * 接口前缀：/accounting/project
 */
@Slf4j
@RestController
@RequestMapping("/accounting/project")
public class ProjectController {

    @Resource
    private ProjectService projectService;

    /**
     * 新增项目
     */
    @PostMapping("/add")
    public ResultVO addProject(@RequestBody Map<String, Object> params) {
        log.info("新增项目参数（原始）：{}", params);
        try {
            // 手动构建Project对象，避免日期转换问题
            Project project = new Project();
            
            // 必填字段验证
            String projectCode = (String) params.get("projectCode");
            String projectName = (String) params.get("projectName");
            
            if (projectName == null || projectName.trim().isEmpty()) {
                return ResultUtil.systemError("项目名称不能为空");
            }
            if (projectCode == null || projectCode.trim().isEmpty()) {
                return ResultUtil.systemError("项目编号不能为空");
            }
            
            // 检查项目编号是否重复
            QueryWrapper<Project> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("project_code", projectCode);
            if (projectService.count(queryWrapper) > 0) {
                return ResultUtil.systemError("项目编号已存在");
            }
            
            // 设置基本字段
            project.setProjectCode(projectCode);
            project.setProjectName(projectName);
            project.setProjectManager((String) params.get("projectManager"));
            project.setDescription((String) params.get("description"));
            
            // 处理状态
            String status = (String) params.get("status");
            if (status == null || status.trim().isEmpty()) {
                project.setStatus("进行中");
            } else {
                project.setStatus(status);
            }
            
            // 处理日期字段（前端发送的是 "yyyy-MM-dd" 格式的字符串）
            try {
                String startDateStr = (String) params.get("startDate");
                if (startDateStr != null && !startDateStr.trim().isEmpty()) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    project.setStartDate(sdf.parse(startDateStr));
                }
                
                String endDateStr = (String) params.get("endDate");
                if (endDateStr != null && !endDateStr.trim().isEmpty()) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    project.setEndDate(sdf.parse(endDateStr));
                }
            } catch (Exception e) {
                log.warn("日期格式转换失败，将设置为null", e);
            }
            
            // 处理预算金额
            Object budgetObj = params.get("budget");
            if (budgetObj != null) {
                if (budgetObj instanceof Number) {
                    project.setBudget(new java.math.BigDecimal(budgetObj.toString()));
                } else if (budgetObj instanceof String && !budgetObj.toString().trim().isEmpty()) {
                    project.setBudget(new java.math.BigDecimal(budgetObj.toString()));
                }
            }
            
            // 补充时间字段
            Date now = new Date();
            project.setCreateTime(now);
            project.setUpdateTime(now);
            
            // 设置创建人ID
            project.setCreateUserId(0L);
            
            log.info("保存前的项目对象：{}", project);
            // 使用自定义的insert方法确保字段映射正确
            boolean success = projectService.save(project);
            log.info("保存结果：{}，项目ID：{}", success, project.getProjectId());
            
            return success ?
                    ResultUtil.success("新增成功", project.getProjectId()) :
                    ResultUtil.systemError("新增失败");
        } catch (Exception e) {
            log.error("新增项目异常", e);
            e.printStackTrace(); // 打印完整堆栈信息
            // 捕获数据库唯一约束异常
            if (e.getMessage() != null && (e.getMessage().contains("Duplicate entry") || e.getMessage().contains("uk_project_code"))) {
                return ResultUtil.systemError("项目编号已存在");
            }
            return ResultUtil.systemError("新增失败：" + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    /**
     * 分页查询项目列表
     */
    @GetMapping("/list")
    public ResultVO getProjectList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String projectName,
            @RequestParam(required = false) String projectCode,
            @RequestParam(required = false) String status
    ) {
        log.info("查询项目列表参数：pageNum={}, pageSize={}, projectName={}, projectCode={}, status={}",
                pageNum, pageSize, projectName, projectCode, status);
        try {
            Page<Project> page = new Page<>(pageNum, pageSize);
            // 使用自定义的分页查询方法，确保字段映射正确
            IPage<Project> result = projectService.selectProjectPage(page, projectName, projectCode, status);
            return ResultUtil.success("查询成功", result);
        } catch (Exception e) {
            log.error("查询项目列表异常", e);
            return ResultUtil.systemError("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询项目详情
     */
    @GetMapping("/detail/{projectId}")
    public ResultVO getProjectDetail(@PathVariable Long projectId) {
        log.info("查询项目详情：projectId={}", projectId);
        try {
            Project project = projectService.getById(projectId);
            if (project == null) {
                return ResultUtil.systemError("项目不存在");
            }
            return ResultUtil.success("查询成功", project);
        } catch (Exception e) {
            log.error("查询项目详情异常", e);
            return ResultUtil.systemError("查询失败：" + e.getMessage());
        }
    }

    /**
     * 修改项目
     */
    @PutMapping("/update")
    public ResultVO updateProject(@RequestBody Project project) {
        log.info("修改项目参数：{}", project);
        try {
            if (project.getProjectId() == null) {
                return ResultUtil.systemError("项目ID不能为空");
            }
            if (project.getProjectName() == null || project.getProjectName().trim().isEmpty()) {
                return ResultUtil.systemError("项目名称不能为空");
            }
            project.setUpdateTime(new Date());
            boolean success = projectService.updateById(project);
            return success ?
                    ResultUtil.success("修改成功", null) :
                    ResultUtil.systemError("修改失败");
        } catch (Exception e) {
            log.error("修改项目异常", e);
            return ResultUtil.systemError("修改失败：" + e.getMessage());
        }
    }

    /**
     * 删除项目（软删除，状态改为"已取消"）
     */
    @DeleteMapping("/delete/{projectId}")
    public ResultVO deleteProject(@PathVariable Long projectId) {
        log.info("删除项目：projectId={}", projectId);
        try {
            Project project = projectService.getById(projectId);
            if (project == null) {
                return ResultUtil.systemError("项目不存在");
            }
            if ("已完成".equals(project.getStatus())) {
                return ResultUtil.systemError("已完成的项目不允许删除");
            }
            
            project.setStatus("已取消"); // 软删除
            project.setUpdateTime(new Date());
            boolean success = projectService.updateById(project);
            return success ?
                    ResultUtil.success("项目已取消", null) :
                    ResultUtil.systemError("取消项目失败");
        } catch (Exception e) {
            log.error("删除项目异常", e);
            return ResultUtil.systemError("取消项目失败：" + e.getMessage());
        }
    }

    /**
     * 切换项目状态
     */
    @PutMapping("/changeStatus")
    public ResultVO changeStatus(
            @RequestParam Long projectId,
            @RequestParam String status
    ) {
        log.info("切换项目状态：projectId={}, status={}", projectId, status);
        try {
            Project project = projectService.getById(projectId);
            if (project == null) {
                return ResultUtil.systemError("项目不存在");
            }
            if ("已取消".equals(project.getStatus())) {
                return ResultUtil.systemError("已取消的项目不允许修改状态");
            }
            if ("已完成".equals(project.getStatus()) && !"已完成".equals(status)) {
                return ResultUtil.systemError("已完成的项目不允许修改为其他状态");
            }
            
            project.setStatus(status);
            project.setUpdateTime(new Date());
            boolean success = projectService.updateById(project);
            return success ?
                    ResultUtil.success("状态切换成功", null) :
                    ResultUtil.systemError("状态切换失败");
        } catch (Exception e) {
            log.error("切换项目状态异常", e);
            return ResultUtil.systemError("状态切换失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有项目列表（用于下拉框）
     */
    @GetMapping("/all")
    public ResultVO getAllProjects() {
        log.info("获取所有项目列表");
        try {
            // 使用自定义的分页查询方法，传入null表示不分页，只查询状态为"进行中"的项目
            Page<Project> page = new Page<>(1, 1000); // 设置一个大的pageSize来获取所有数据
            IPage<Project> result = projectService.selectProjectPage(page, null, null, "进行中");
            return ResultUtil.success("查询成功", result.getRecords());
        } catch (Exception e) {
            log.error("获取所有项目列表异常", e);
            return ResultUtil.systemError("查询失败：" + e.getMessage());
        }
    }
}

