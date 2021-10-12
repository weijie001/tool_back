package com.tool.controller;

import com.tool.config.ToolConfig;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseController {

    @Autowired
    protected ToolConfig toolConfig;
}
