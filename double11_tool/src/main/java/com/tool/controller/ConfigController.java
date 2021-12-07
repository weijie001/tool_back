package com.tool.controller;

import com.tool.bean.AccountInfo;
import com.tool.bean.ChooseServerInfo;
import com.tool.bean.TestAccount;
import com.tool.dao.TestAccountDao;
import com.tool.http.OkhttpUtils;
import com.tool.util.FileUtil;
import com.tool.util.ShellUtil;
import net.galasports.support.pub.bean.proto.BaseProtos;
import net.galasports.support.pub.bean.proto.GameInfoProtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequestMapping("config")
@RestController
public class ConfigController extends BaseController{

    private static final Logger log = LoggerFactory.getLogger(ConfigController.class);

    @Value("${file.path}")
    public  String projectDir;

    @Autowired
    private TestAccountDao testAccountDao;

    @RequestMapping("uploadFile")
    public boolean uploadFile(@RequestParam String dir, @RequestParam("file") MultipartFile file) {
        return FileUtil.upload(file, dir, projectDir);
    }

    @RequestMapping("getAccount")
    public AccountInfo getAccount() throws IOException {
        List<TestAccount> allTestAccount = testAccountDao.getAllTestAccount();
        GameInfoProtos.GameServerReq build = GameInfoProtos.GameServerReq.newBuilder().setGame(toolConfig.getGameType()).setChannel(toolConfig.getChannel()).setChannelInfo(toolConfig.getChannelInfo()).build();
        byte[] bytes = OkhttpUtils.request(toolConfig.getChooseServerUrl(), build.toByteArray());
        GameInfoProtos.GameServerRes gameServerRes = GameInfoProtos.GameServerRes.parseFrom(bytes);
        List<BaseProtos.GameServerInfoPB> gameServerInfoList = gameServerRes.getGameServerInfoList();
        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setTestAccounts(allTestAccount);
        List<ChooseServerInfo> chooseServerInfos = new ArrayList<>();
        for (BaseProtos.GameServerInfoPB gameServerInfoPB : gameServerInfoList) {
            ChooseServerInfo chooseServerInfo = new ChooseServerInfo();
            chooseServerInfo.setServerId(gameServerInfoPB.getServerId());
            chooseServerInfo.setAddr(gameServerInfoPB.getAddr());
            chooseServerInfo.setName(gameServerInfoPB.getName());
            chooseServerInfo.setPort(gameServerInfoPB.getPort());
            chooseServerInfos.add(chooseServerInfo);
        }
        accountInfo.setChooseServerInfos(chooseServerInfos);
        return accountInfo;
    }

    @RequestMapping("addAccount")
    public int addAccount(@RequestParam String account,@RequestParam String token,@RequestParam String env) {
        return testAccountDao.add(account,env,"","");
    }

    @RequestMapping("addAccount2")
    public int addAccount2(@RequestParam String account,@RequestParam String env,@RequestParam String serverId,@RequestParam String name) {
        return testAccountDao.add(account,env,serverId,name);
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
