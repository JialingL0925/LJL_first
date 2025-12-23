package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.model.entity.Employee;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * 员工Mapper（手机号登录专用，使用XML映射避免字段映射问题）
 */
@Repository
public interface EmployeeMapper extends BaseMapper<Employee> {

    /**
     * 按手机号查询在职员工（登录核心查询）
     * @param phone 登录手机号
     * @return 在职员工信息（null=手机号不存在/非在职）
     */
    Employee selectByPhone(@Param("phone") String phone);
    
    /**
     * 按手机号查询员工（不限制状态，用于检查手机号是否已存在）
     * @param phone 手机号
     * @return 员工信息（null=手机号不存在）
     */
    Employee selectByPhoneAnyStatus(@Param("phone") String phone);
    
    /**
     * 分页查询员工列表（自定义查询，避免字段映射问题）
     * @param page 分页对象
     * @param name 姓名（模糊查询）
     * @param phone 手机号（模糊查询）
     * @param status 状态（精确查询）
     * @return 分页结果
     */
    IPage<Employee> selectEmployeePage(Page<Employee> page, @Param("name") String name, @Param("phone") String phone, @Param("status") String status);
    
    /**
     * 根据ID查询员工详情（自定义查询，避免字段映射问题）
     * @param employeeId 员工ID
     * @return 员工信息
     */
    Employee selectById(@Param("employeeId") Long employeeId);
    
    /**
     * 根据ID更新员工信息（自定义更新，避免字段映射问题）
     * @param employee 员工对象
     * @return 更新行数
     */
    int updateById(Employee employee);
    
    /**
     * 新增员工（自定义插入，避免字段映射问题）
     * @param employee 员工对象
     * @return 插入行数
     */
    int insert(Employee employee);
}