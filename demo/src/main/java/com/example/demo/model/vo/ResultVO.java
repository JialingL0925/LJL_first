package com.example.demo.model.vo;

import lombok.Data;

@Data
public class ResultVO {
    private int code;       // 状态码（200成功，401权限错误，500系统错误）
    private String msg;     // 提示消息
    private Object data;    // 响应数据
    
    // 1. 必须添加无参构造函数（Jackson序列化必备）
    public ResultVO() {
    }

    // 2. 保留带参构造函数（兼容手动创建）
    public ResultVO(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
    
    // 动态计算success值的getter方法
    public boolean isSuccess() {
        return code == 200;
    }

    // 3. 新增静态快捷方法（替代ResultUtil，简化调用，可选但推荐）
    public static ResultVO success(String msg, Object data) {
        return new ResultVO(200, msg, data);
    }

    public static ResultVO error(String msg) {
        return new ResultVO(401, msg, null);
    }

    public static ResultVO error(int code, String msg) {
        return new ResultVO(code, msg, null);
    }
}