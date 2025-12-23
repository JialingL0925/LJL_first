package com.example.demo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.mapper.EmployeeMapper;
import com.example.demo.model.entity.Employee;
import com.example.demo.model.entity.Role;
import com.example.demo.model.vo.ResultVO;
import com.example.demo.service.EmployeeService;
import com.example.demo.service.RoleService;
import com.example.demo.util.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 员工管理控制器
 * 仅管理员可访问
 */
@Slf4j
@RestController
@RequestMapping("/accounting/employee")
public class EmployeeController {

    @Resource
    private EmployeeService employeeService;

    @Resource
    private EmployeeMapper employeeMapper;

    @Resource
    private RoleService roleService;

    /**
     * 分页查询员工列表
     */
    @GetMapping("/list")
    public ResultVO getEmployeeList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String status
    ) {
        log.info("查询员工列表参数：pageNum={}, pageSize={}, name={}, phone={}, status={}",
                pageNum, pageSize, name, phone, status);
        try {
            Page<Employee> page = new Page<>(pageNum, pageSize);
            
            // 使用自定义Mapper方法，避免MyBatis-Plus自动生成SQL时的字段映射问题
            IPage<Employee> pageResult = employeeMapper.selectEmployeePage(
                    page,
                    name != null && !name.trim().isEmpty() ? name.trim() : null,
                    phone != null && !phone.trim().isEmpty() ? phone.trim() : null,
                    status != null && !status.trim().isEmpty() ? status.trim() : null
            );

            // 查询所有角色，用于显示角色名称
            List<Role> allRoles = roleService.list();
            Map<Long, String> roleMap = allRoles.stream()
                    .collect(Collectors.toMap(Role::getRoleId, Role::getRoleName));

            // 为每个员工添加角色名称
            List<Map<String, Object>> employeeList = pageResult.getRecords().stream().map(emp -> {
                Map<String, Object> empMap = new HashMap<>();
                empMap.put("employeeId", emp.getEmployeeId());
                empMap.put("name", emp.getName());
                empMap.put("phone", emp.getPhone());
                empMap.put("status", emp.getStatus());
                empMap.put("roleId", emp.getRoleId());
                empMap.put("roleName", roleMap.getOrDefault(emp.getRoleId(), "未知角色"));
                return empMap;
            }).collect(Collectors.toList());

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("records", employeeList);
            result.put("total", pageResult.getTotal());
            result.put("size", pageResult.getSize());
            result.put("current", pageResult.getCurrent());
            result.put("pages", pageResult.getPages());

            log.info("查询员工列表成功，总数：{}", pageResult.getTotal());
            return ResultUtil.success("查询成功", result);
        } catch (Exception e) {
            log.error("查询员工列表异常", e);
            return ResultUtil.systemError("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有角色列表（用于下拉选择）
     */
    @GetMapping("/roles")
    public ResultVO getAllRoles() {
        try {
            List<Role> roles = roleService.list();
            log.info("查询角色列表成功，数量：{}", roles.size());
            return ResultUtil.success("查询成功", roles);
        } catch (Exception e) {
            log.error("查询角色列表异常", e);
            return ResultUtil.systemError("查询失败：" + e.getMessage());
        }
    }

    /**
     * 新增员工
     */
    @PostMapping("/add")
    public ResultVO addEmployee(@RequestBody Map<String, Object> params) {
        log.info("新增员工参数：{}", params);
        try {
            // 获取参数
            String name = params.get("name") != null ? params.get("name").toString().trim() : null;
            String phone = params.get("phone") != null ? params.get("phone").toString().trim() : null;
            String password = params.get("password") != null ? params.get("password").toString() : null;
            String status = params.get("status") != null ? params.get("status").toString().trim() : "在职";
            String roleIdStr = params.get("roleIdStr") != null ? params.get("roleIdStr").toString().trim() : null;

            // 参数验证
            if (name == null || name.isEmpty()) {
                return ResultUtil.error("员工姓名不能为空");
            }
            if (phone == null || phone.isEmpty()) {
                return ResultUtil.error("手机号不能为空");
            }
            if (password == null || password.isEmpty()) {
                return ResultUtil.error("密码不能为空");
            }

            // 检查手机号是否已存在（使用自定义Mapper方法，避免字段映射问题）
            Employee existing = employeeMapper.selectByPhoneAnyStatus(phone);
            if (existing != null) {
                return ResultUtil.error("该手机号已被使用");
            }

            // 创建新员工
            Employee employee = new Employee();
            employee.setName(name);
            employee.setPhone(phone);
            employee.setPassword(password); // 密码明文存储（根据现有逻辑）
            employee.setStatus(status);

            // 转换角色ID
            if (roleIdStr != null && !roleIdStr.isEmpty()) {
                try {
                    Long roleId = Long.parseLong(roleIdStr);
                    employee.setRoleId(roleId);
                } catch (NumberFormatException e) {
                    return ResultUtil.error("角色ID格式错误");
                }
            }

            // 使用自定义Mapper方法，避免MyBatis-Plus自动生成SQL时的字段映射问题
            int inserted = employeeMapper.insert(employee);
            if (inserted > 0) {
                log.info("新增员工成功：{}", employee.getEmployeeId());
                return ResultUtil.success("新增员工成功");
            } else {
                return ResultUtil.error("新增员工失败");
            }
        } catch (Exception e) {
            log.error("新增员工异常", e);
            return ResultUtil.systemError("新增失败：" + e.getMessage());
        }
    }

    /**
     * 修改员工信息
     */
    @PutMapping("/update")
    public ResultVO updateEmployee(@RequestBody Map<String, Object> params) {
        log.info("修改员工参数：{}", params);
        try {
            // 获取员工ID
            Long employeeId = null;
            if (params.get("employeeId") != null) {
                if (params.get("employeeId") instanceof Number) {
                    employeeId = ((Number) params.get("employeeId")).longValue();
                } else {
                    employeeId = Long.parseLong(params.get("employeeId").toString());
                }
            }
            
            if (employeeId == null) {
                return ResultUtil.error("员工ID不能为空");
            }

            // 使用自定义Mapper方法，避免MyBatis-Plus自动生成SQL时的字段映射问题
            Employee employee = employeeMapper.selectById(employeeId);
            if (employee == null) {
                return ResultUtil.error("员工不存在");
            }

            // 获取参数
            String name = params.get("name") != null ? params.get("name").toString().trim() : null;
            String phone = params.get("phone") != null ? params.get("phone").toString().trim() : null;
            String password = params.get("password") != null ? params.get("password").toString() : null;
            String status = params.get("status") != null ? params.get("status").toString().trim() : null;
            String roleIdStr = params.get("roleIdStr") != null ? params.get("roleIdStr").toString().trim() : null;

            // 如果修改手机号，检查是否重复（使用自定义Mapper方法，避免字段映射问题）
            if (phone != null && !phone.isEmpty() && !phone.equals(employee.getPhone())) {
                Employee existing = employeeMapper.selectByPhoneAnyStatus(phone);
                if (existing != null && !existing.getEmployeeId().equals(employeeId)) {
                    return ResultUtil.error("该手机号已被其他员工使用");
                }
                employee.setPhone(phone);
            }

            // 更新其他字段
            if (name != null && !name.isEmpty()) {
                employee.setName(name);
            }
            if (password != null && !password.isEmpty()) {
                employee.setPassword(password); // 密码明文存储（根据现有逻辑）
            }
            if (status != null && !status.isEmpty()) {
                employee.setStatus(status);
            }
            if (roleIdStr != null && !roleIdStr.isEmpty()) {
                try {
                    Long roleId = Long.parseLong(roleIdStr);
                    employee.setRoleId(roleId);
                } catch (NumberFormatException e) {
                    return ResultUtil.error("角色ID格式错误");
                }
            }

            // 使用自定义Mapper方法，避免MyBatis-Plus自动生成SQL时的字段映射问题
            int updated = employeeMapper.updateById(employee);
            if (updated > 0) {
                log.info("修改员工成功：{}", employeeId);
                return ResultUtil.success("修改员工成功");
            } else {
                return ResultUtil.error("修改员工失败");
            }
        } catch (Exception e) {
            log.error("修改员工异常", e);
            return ResultUtil.systemError("修改失败：" + e.getMessage());
        }
    }

    /**
     * 删除员工（设置为停用状态）
     */
    @DeleteMapping("/delete/{employeeId}")
    public ResultVO deleteEmployee(@PathVariable Long employeeId) {
        log.info("删除员工：employeeId={}", employeeId);
        try {
            // 使用自定义Mapper方法，避免MyBatis-Plus自动生成SQL时的字段映射问题
            Employee employee = employeeMapper.selectById(employeeId);
            if (employee == null) {
                return ResultUtil.error("员工不存在");
            }

            // 检查是否为管理员角色，管理员不能被删除
            if (employee.getRoleId() != null) {
                Role role = roleService.getById(employee.getRoleId());
                if (role != null && "ADMIN".equals(role.getRoleName())) {
                    return ResultUtil.error("管理员角色不能被删除");
                }
                // 如果role_id为1，也认为是管理员（兼容处理）
                if (employee.getRoleId() == 1L) {
                    return ResultUtil.error("管理员角色不能被删除");
                }
            }

            // 软删除：设置为停用状态（保留数据，不真实删除）
            employee.setStatus("停用");
            // 使用自定义Mapper方法，避免MyBatis-Plus自动生成SQL时的字段映射问题
            int updated = employeeMapper.updateById(employee);
            if (updated > 0) {
                log.info("停用员工成功：{}", employeeId);
                return ResultUtil.success("员工已停用");
            } else {
                return ResultUtil.error("停用员工失败");
            }
        } catch (Exception e) {
            log.error("删除员工异常", e);
            return ResultUtil.systemError("删除失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询员工详情
     */
    @GetMapping("/detail/{employeeId}")
    public ResultVO getEmployeeDetail(@PathVariable Long employeeId) {
        log.info("查询员工详情：employeeId={}", employeeId);
        try {
            // 使用自定义Mapper方法，避免MyBatis-Plus自动生成SQL时的字段映射问题
            Employee employee = employeeMapper.selectById(employeeId);
            if (employee == null) {
                return ResultUtil.error("员工不存在");
            }

            // 查询角色名称
            String roleName = "未知角色";
            if (employee.getRoleId() != null) {
                Role role = roleService.getById(employee.getRoleId());
                if (role != null) {
                    roleName = role.getRoleName();
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("employeeId", employee.getEmployeeId());
            result.put("name", employee.getName());
            result.put("phone", employee.getPhone());
            result.put("status", employee.getStatus());
            result.put("roleId", employee.getRoleId());
            result.put("roleName", roleName);

            return ResultUtil.success("查询成功", result);
        } catch (Exception e) {
            log.error("查询员工详情异常", e);
            return ResultUtil.systemError("查询失败：" + e.getMessage());
        }
    }
}

