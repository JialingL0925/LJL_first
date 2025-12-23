package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.mapper.EmployeeMapper;
import com.example.demo.mapper.RoleMapper;
import com.example.demo.model.entity.Employee;
import com.example.demo.model.entity.Role;
import com.example.demo.model.vo.ResultVO;
import com.example.demo.service.EmployeeService;
import com.example.demo.util.JwtUtil;
import com.example.demo.util.ResultUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 员工服务实现类（依赖独立配置的密码编码器，解决循环依赖）
 */
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
    // 1. 添加日志对象（核心：打印关键步骤，定位问题）
    private static final Logger log = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    // 依赖独立配置的密码编码器（来自PasswordConfig，非SecurityConfig）
    // 仅注释注入：不用加密器，保留变量避免删改
    // @Autowired  // 优先使用@Autowired，更适配Spring依赖注入机制
    private PasswordEncoder passwordEncoder;

    @Autowired  // 统一使用@Autowired，保持注入方式一致
    private JwtUtil jwtUtil;

    @Autowired
    private RoleMapper roleMapper;

    // 登录核心逻辑（补充状态校验，与数据库status字段对齐）
    @Override
    public ResultVO login(String phone, String password) {
        // 2. 补充参数非空校验（避免空指针）
        log.info("开始处理登录请求 → 手机号：{}，密码：{}", phone, password);
        if (phone == null || phone.trim().isEmpty()) {
            log.warn("登录失败 → 手机号为空");
            return ResultUtil.error("手机号不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            log.warn("登录失败 → 密码为空，手机号：{}", phone);
            return ResultUtil.error("密码不能为空");
        }

        // 3. 按手机号查询员工（需确保EmployeeMapper存在selectByPhone方法）
        Employee employee = baseMapper.selectByPhone(phone);
        log.info("查询员工结果 → 手机号：{}，查询到员工：{}", phone, employee == null ? "不存在" : employee.getName());
        if (employee != null) {
            log.info("员工详细信息 → employeeId：{}，name：{}，phone：{}，roleId：{}", 
                    employee.getEmployeeId(), employee.getName(), employee.getPhone(), employee.getRoleId());
        }

        // 校验：员工不存在 或 状态非"在职"（与数据库status字段值匹配）
        if (employee == null || !"在职".equals(employee.getStatus())) {
            log.warn("登录失败 → 员工不存在或已停用，手机号：{}，查询结果：{}", phone, employee);
            return ResultUtil.error("员工不存在或已停用");
        }

        // 4. 密码验证（仅改这一行：明文对比，完全不用加密器）
        boolean passwordMatch = password.equals(employee.getPassword());
        log.info("密码对比结果 → 手机号：{}，数据库密码：{}，传入密码：{}，是否匹配：{}",
                phone, employee.getPassword(), password, passwordMatch);
        if (!passwordMatch) {
            return ResultUtil.error("密码错误");
        }

        // 5. 查询角色信息
        Role role = null;
        String roleName = "ADMIN";  // 默认角色
        log.info("查询角色信息 → 员工role_id：{}", employee.getRoleId());
        if (employee.getRoleId() != null) {
            try {
                // 尝试查询角色
                role = roleMapper.selectById(employee.getRoleId());
                log.info("查询角色结果 → role_id：{}，角色对象：{}", employee.getRoleId(), role);
                
                // 如果查询结果为null，尝试直接查询所有角色看看
                if (role == null) {
                    log.warn("⚠️ selectById返回null，尝试查询所有角色...");
                    java.util.List<Role> allRoles = roleMapper.selectList(null);
                    log.info("所有角色列表：{}", allRoles);
                    
                    // 手动查找匹配的角色
                    for (Role r : allRoles) {
                        if (r.getRoleId() != null && r.getRoleId().equals(employee.getRoleId())) {
                            role = r;
                            log.info("✅ 手动找到匹配的角色：{}", role);
                            break;
                        }
                    }
                }
                
                if (role != null) {
                    roleName = role.getRoleName();
                    log.info("获取角色名称 → roleName：{}", roleName);
                } else {
                    log.error("❌ 角色不存在！role_id：{}，使用默认角色ADMIN", employee.getRoleId());
                }
            } catch (Exception e) {
                log.error("查询角色异常 → role_id：{}，异常：{}", employee.getRoleId(), e.getMessage(), e);
            }
        } else {
            log.warn("⚠️ 员工role_id为空，使用默认角色ADMIN");
        }

        // 6. 生成JWT Token（加异常捕获，避免Token生成失败导致响应体为空）
        String token = null;
        try {
            UserDetails userDetails = User.withUsername(employee.getPhone())
                    .password(employee.getPassword())
                    .authorities(roleName)  // 根据角色设置权限
                    .build();
            token = jwtUtil.generateToken(userDetails, employee.getEmployeeId(), roleName);
            log.info("Token生成成功 → 手机号：{}，角色：{}，Token：{}", phone, roleName, token);
        } catch (Exception e) {
            log.error("Token生成失败 → 手机号：{}，异常信息：{}", phone, e.getMessage(), e);
            return ResultUtil.error("登录失败：Token生成异常，请联系管理员");
        }

        // 7. 返回登录成功结果（包含Token和角色信息）
        log.info("登录成功 → 手机号：{}，员工姓名：{}，员工role_id：{}，角色名称：{}", 
                phone, employee.getName(), employee.getRoleId(), roleName);
        
        // 验证返回的角色信息是否正确
        if (employee.getRoleId() != null && !employee.getRoleId().equals(1L) && "ADMIN".equals(roleName)) {
            log.error("❌ 角色信息异常！员工role_id：{}，但返回的角色名称是ADMIN，这不应该发生！", employee.getRoleId());
        }
        
        Map<String, Object> loginResult = new HashMap<>();
        loginResult.put("token", token);
        loginResult.put("role", roleName);
        loginResult.put("roleId", employee.getRoleId());
        
        log.info("返回登录结果 → role：{}，roleId：{}", roleName, employee.getRoleId());
        return ResultUtil.success("登录成功", loginResult);
    }

    // 注册核心逻辑
    @Override
    public ResultVO register(String name, String phone, String password, Long roleId) {
        log.info("开始处理注册请求 → 姓名：{}，手机号：{}，角色ID：{}", name, phone, roleId);
        
        // 1. 参数校验
        if (name == null || name.trim().isEmpty()) {
            log.warn("注册失败 → 姓名为空");
            return ResultUtil.error("姓名不能为空");
        }
        if (phone == null || phone.trim().isEmpty()) {
            log.warn("注册失败 → 手机号为空");
            return ResultUtil.error("手机号不能为空");
        }
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            log.warn("注册失败 → 手机号格式不正确：{}", phone);
            return ResultUtil.error("手机号格式不正确");
        }
        if (password == null || password.trim().isEmpty()) {
            log.warn("注册失败 → 密码为空，手机号：{}", phone);
            return ResultUtil.error("密码不能为空");
        }
        if (password.length() < 6) {
            log.warn("注册失败 → 密码长度不足，手机号：{}", phone);
            return ResultUtil.error("密码长度不能少于6位");
        }
        
        // 2. 角色验证：禁止注册管理员角色（roleId=1）
        if (roleId == null) {
            log.warn("注册失败 → 角色ID为空，手机号：{}", phone);
            return ResultUtil.error("请选择角色");
        }
        if (roleId == 1L) {
            log.warn("注册失败 → 禁止注册管理员角色，手机号：{}", phone);
            return ResultUtil.error("管理员角色不能通过注册获得，请联系系统管理员");
        }
        
        // 3. 验证角色是否存在
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            log.warn("注册失败 → 角色不存在，角色ID：{}，手机号：{}", roleId, phone);
            return ResultUtil.error("选择的角色不存在");
        }

        // 4. 检查手机号是否已存在（查询所有状态的员工，因为手机号唯一）
        try {
            List<Employee> existingEmployees = baseMapper.selectList(
                    new LambdaQueryWrapper<Employee>().eq(Employee::getPhone, phone)
            );
            if (existingEmployees != null && !existingEmployees.isEmpty()) {
                log.warn("注册失败 → 手机号已存在：{}", phone);
                return ResultUtil.error("该手机号已被注册");
            }
        } catch (Exception e) {
            log.warn("查询手机号是否存在时发生异常，将继续注册流程：{}", e.getMessage());
            // 如果查询失败，继续注册流程（数据库唯一约束会处理重复）
        }

        // 5. 创建新员工（密码明文存储，与登录逻辑保持一致）
        Employee newEmployee = new Employee();
        newEmployee.setName(name.trim());
        newEmployee.setPhone(phone.trim());
        newEmployee.setPassword(password); // 明文存储
        newEmployee.setStatus("在职");
        newEmployee.setRoleId(roleId); // 使用传入的角色ID

        // 6. 保存到数据库
        try {
            boolean saved = this.save(newEmployee);
            if (!saved) {
                log.error("注册失败 → 保存员工失败，手机号：{}", phone);
                return ResultUtil.error("注册失败，请稍后重试");
            }
            log.info("注册成功 → 手机号：{}，员工姓名：{}，角色：{}，员工ID：{}", phone, name, role.getRoleName(), newEmployee.getEmployeeId());
            return ResultUtil.success("注册成功，请登录", null);
        } catch (Exception e) {
            log.error("注册失败 → 保存员工异常，手机号：{}，异常信息：{}", phone, e.getMessage(), e);
            return ResultUtil.error("注册失败：" + e.getMessage());
        }
    }

    // Security加载用户信息（Token验证时调用，补充状态校验）
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Security加载用户信息 → 用户名（手机号）：{}", username);
        // username为登录手机号
        Employee employee = baseMapper.selectByPhone(username);
        if (employee == null || !"在职".equals(employee.getStatus())) {
            log.warn("Security加载用户失败 → 用户不存在或已停用，手机号：{}", username);
            throw new UsernameNotFoundException("用户不存在或已停用");
        }

        // 查询角色信息
        Role role = null;
        String roleName = "ADMIN";  // 默认角色
        log.info("Security查询角色信息 → 员工role_id：{}", employee.getRoleId());
        if (employee.getRoleId() != null) {
            try {
                // 尝试查询角色
                role = roleMapper.selectById(employee.getRoleId());
                log.info("Security查询角色结果 → role_id：{}，角色对象：{}", employee.getRoleId(), role);
                
                // 如果查询结果为null，尝试直接查询所有角色看看
                if (role == null) {
                    log.warn("⚠️ Security selectById返回null，尝试查询所有角色...");
                    List<Role> allRoles = roleMapper.selectList(null);
                    log.info("Security所有角色列表：{}", allRoles);
                    
                    // 手动查找匹配的角色
                    for (Role r : allRoles) {
                        if (r.getRoleId() != null && r.getRoleId().equals(employee.getRoleId())) {
                            role = r;
                            log.info("✅ Security手动找到匹配的角色：{}", role);
                            break;
                        }
                    }
                }
                
                if (role != null) {
                    roleName = role.getRoleName();
                    log.info("Security获取角色名称 → roleName：{}", roleName);
                } else {
                    log.error("❌ Security角色不存在！role_id：{}，使用默认角色ADMIN", employee.getRoleId());
                }
            } catch (Exception e) {
                log.error("Security查询角色异常 → role_id：{}，异常：{}", employee.getRoleId(), e.getMessage(), e);
            }
        } else {
            log.warn("⚠️ Security员工role_id为空，使用默认角色ADMIN");
        }

        // 返回用户详情（用于Security权限校验）
        log.info("Security加载用户成功 → 手机号：{}，员工姓名：{}，角色：{}", username, employee.getName(), roleName);
        return User.withUsername(employee.getPhone())
                .password(employee.getPassword())
                .authorities(roleName)
                .build();
    }
}