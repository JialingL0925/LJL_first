package com.example.demo.util;

import com.example.demo.model.vo.ResultVO;

/**
 * 响应结果工具类（与ResultVO配套，统一管理响应状态码）
 */
public class ResultUtil {
    // ========== 通用状态码定义（规范） ==========
    private static final int SUCCESS_CODE = 200;    // 业务成功
    private static final int LOGIN_FAIL_CODE = 401; // 登录失败（密码错误/用户不存在）
    private static final int SYSTEM_ERROR_CODE = 500; // 系统异常

    // ========== 成功响应 ==========
    /**
     * 带消息+数据的成功响应（登录成功、查询成功等）
     */
    public static ResultVO success(String msg, Object data) {
        return new ResultVO(SUCCESS_CODE, msg, data);
    }

    /**
     * 简化版成功响应（仅消息，无数据）
     */
    public static ResultVO success(String msg) {
        return new ResultVO(SUCCESS_CODE, msg, null);
    }

    // ========== 失败响应 ==========
    /**
     * 登录相关失败（密码错误/用户不存在/已停用）→ 用401码（而非500）
     */
    public static ResultVO loginError(String msg) {
        return new ResultVO(LOGIN_FAIL_CODE, msg, null);
    }

    /**
     * 系统异常失败（数据库错误、Token生成失败等）→ 用500码
     */
    public static ResultVO systemError(String msg) {
        return new ResultVO(SYSTEM_ERROR_CODE, msg, null);
    }

    /**
     * 兼容原error方法（避免修改现有代码，映射为登录失败）
     * 注：原代码中error用了500，这里改为401更合理，前端能精准判定
     */
    public static ResultVO error(String msg) {
        return loginError(msg);
    }
}