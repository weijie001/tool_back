package com.tool.bean;

import lombok.Data;

/**
 * 统一返回
 * @author weijie 2021.11
 */
@Data
public class ResponseResult<T> {
    /**
     * 返回码
     */
    private int ret;
    /**
     * 数据
     */
    private T data;
    /**
     * 信息
     */
    private String message;

    public ResponseResult(int ret, T data, String message) {
        this.ret = ret;
        this.data = data;
        this.message = message;
    }
    public ResponseResult(int ret, String message) {
        this.ret = ret;
        this.message = message;
    }
}
