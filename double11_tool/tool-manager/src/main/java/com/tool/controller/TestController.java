package com.tool.controller;

import com.tool.config.ToolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("testC")
@RestController
public class TestController {
    @Autowired
    private ToolConfig toolConfig;

    @GetMapping("/test")
    public Object test() {
        return toolConfig.getGameType();
    }
}
