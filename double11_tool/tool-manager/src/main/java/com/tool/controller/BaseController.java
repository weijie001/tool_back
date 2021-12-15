package com.tool.controller;

import com.tool.bean.ResponseResult;
import com.tool.config.ToolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

public class BaseController {

    @Autowired
    protected ToolConfig toolConfig;

    @ExceptionHandler
    public ResponseResult<String> exp(HttpServletRequest request, Exception exp) {
        String message = exp.getMessage();
        exp.printStackTrace();
        return new ResponseResult<>(1, message);
    }
}
