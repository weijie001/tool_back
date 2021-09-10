package com.tool.controller;

import com.tool.bean.Config;
import com.tool.dao.ConfigDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mainC")
public class MainController {

    @Autowired
    protected ConfigDao configDao;

    @RequestMapping("getEnv")
    public Config getEnv() {
        return configDao.getEnv();
    }

    @RequestMapping("changeEnv")
    public int changeEnv(@RequestParam String tag) {
        return  configDao.changeEnv(tag);
    }
}
