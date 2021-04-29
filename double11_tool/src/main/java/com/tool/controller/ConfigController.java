package com.tool.controller;

import com.tool.bean.TestAccount;
import com.tool.dao.TestAccountDao;
import com.tool.service.TestService;
import com.tool.util.FileUtil;
import com.tool.util.ShellUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequestMapping("config")
@RestController
public class ConfigController {
    private static final Logger log = LoggerFactory.getLogger(ConfigController.class);

    @Value("${file.path}")
    public  String projectDir;

    @Autowired
    private TestAccountDao testAccountDao;
    @Autowired
    private TestService testService;
    @RequestMapping("uploadFile")
    public boolean uploadFile(@RequestParam String dir, @RequestParam("file") MultipartFile file) {
        return FileUtil.upload(file, dir, projectDir);
    }
    @RequestMapping("getAccount")
    public List<TestAccount> getAccount() {
        return testAccountDao.getAllTestAccount();
    }

    @RequestMapping("addAccount")
    public int addAccount(@RequestParam String account,@RequestParam String token,@RequestParam String env) {
        return testAccountDao.add(account,token,env);
    }
    @RequestMapping("deleteAccount")
    public void deleteAccount(@RequestParam int id) {
        testAccountDao.delete(id);
    }
    @RequestMapping("rebootServer")
    public int rebootServer() throws InterruptedException, IOException {
        String commandStr = "sh "+projectDir+"restart.sh";
        Process process = Runtime.getRuntime().exec(ShellUtil.getCommand(commandStr));
        int result = process.waitFor();
        log.info("server reboot result:{}",result);
        return result;
    }
}
