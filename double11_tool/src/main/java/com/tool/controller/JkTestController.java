package com.tool.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;
import com.tool.bean.FileNameInfo;
import com.tool.bean.FileTypeEnum;
import com.tool.constant.Constant;
import com.tool.dao.ConfigDao;
import com.tool.dao.TeamDao;
import com.tool.file.FileToolUtil;
import com.tool.http.OkhttpUtils;
import com.tool.http.RequestComponent;
import com.tool.poi.ExcelUtil;
import com.tool.util.FileUtil;
import com.tool.util.JdbcUtil;
import com.tool.util.ShellUtil;
import net.galasports.account.bean.ErrorCode;
import net.galasports.account.bean.protocol.UserProtos;
import net.galasports.support.pub.bean.proto.BaseProtos;
import net.galasports.support.pub.bean.proto.GameInfoProtos;
import net.tool.protocol.ChooseServerProtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 接口配置
 */
@RestController
@RequestMapping("/jk")
public class JkTestController {
    private static final Logger log = LoggerFactory.getLogger(JkTestController.class);

    @Value("${file.path}")
    public String filePath;

    private static final String CONFIG_FILE = "config";

    private static final String PROTO_FILE = "proto";

    @Autowired
    private TeamDao teamDao;

    @Autowired
    ConfigDao configDao;
    /**
     * 获取配置文件
     *
     * @return
     */
    @RequestMapping("/getConfigFile")
    public List<FileNameInfo> getConfigFile() {
        log.info("get config proto file!");
        File configFile = new File(filePath + CONFIG_FILE + File.separator);
        File protoFile = new File(filePath + PROTO_FILE + File.separator);
        List<String> configFileNames = new ArrayList<>();
        List<String> protoFileNames = new ArrayList<>();
        File[] configFiles = configFile.listFiles();
        File[] protoFiles = protoFile.listFiles();
        if (configFiles != null) {
            for (File f : configFiles) {
                String fileName = f.getName();
                configFileNames.add(fileName);
            }
        }
        if (protoFiles != null) {
            for (File f : protoFiles) {
                String fileName = f.getName();
                protoFileNames.add(fileName);
            }
        }
        List<FileNameInfo> fileNameInfos = new ArrayList<>();
        FileNameInfo fileNameInfo = new FileNameInfo();
        fileNameInfo.setType(FileTypeEnum.CONFIG.getType());
        fileNameInfo.setFileNames(configFileNames);
        fileNameInfos.add(fileNameInfo);

        FileNameInfo protoFileNameInfo = new FileNameInfo();
        protoFileNameInfo.setType(FileTypeEnum.PROTO.getType());
        protoFileNameInfo.setFileNames(protoFileNames);
        fileNameInfos.add(protoFileNameInfo);
        return fileNameInfos;
    }

    /**
     * 查询输入参数
     *
     * @param req
     * @return
     * @throws IOException
     */
    @RequestMapping("/getInputParam")
    public String getParam(@RequestParam String req) throws IOException {
        log.info("get input param!");
        return FileToolUtil.getContent(req, filePath);
    }

    /**
     * 通过接口文档获取数据
     *
     * @return
     */
    @GetMapping("/select")
    public JSONArray select() {
        JSONObject excelData = ExcelUtil.getExcelData(filePath);
        if (excelData == null) {
            log.info("has no data!");
            return null;
        }
        return (JSONArray) excelData.get(Constant.SHEET_NAME);
    }

    private String getUrl(String env) {
        return "http://" + env + "/";
    }

    private UserProtos.UserRes loginPlatform(String account) throws IOException {
        UserProtos.LoginReq.Builder loginReq = UserProtos.LoginReq.newBuilder();
        loginReq.setAccount(account);
        loginReq.setPwd(DigestUtils.md5DigestAsHex(Constant.DEFAULT_PASSWORD.getBytes(StandardCharsets.UTF_8)));
        loginReq.setMac("");
        loginReq.setIdfa("");
        loginReq.setGameTypeId(Constant.GAME_TYPE);
        loginReq.setSystem(Constant.SYSTEM);
        byte[] bytes = OkhttpUtils.request(Constant.LOGIN_URL, loginReq.build().toByteArray());
        return UserProtos.UserRes.parseFrom(bytes);
    }

    @GetMapping("/getData")
    public String getData(@RequestParam String req, @RequestParam String res, @RequestParam String uri, @RequestParam(defaultValue = "{}") String data,
                          @RequestParam String account, @RequestParam String env, @RequestParam String serverId) throws Exception {
        log.info("login!");
        String url = getUrl(env);
        UserProtos.UserRes userRes = loginPlatform(account);
        login(userRes.getAccountId(), userRes.getToken(), url,serverId);
        log.info("get data!");
        return send(uri, req, res, data, url);
    }


    @GetMapping("/getTeamInfo")
    public Map<String, Object> getTeamInfo(@RequestParam String teamId) throws Exception {
        String evnTag = configDao.getEvnTag();
        JdbcTemplate devGameJdbc = JdbcUtil.getDefaultGameJdbc(evnTag);
        return teamDao.getTeam(devGameJdbc, teamId);
    }
    @GetMapping("/loginValid2")
    public String loginValid2(@RequestParam String account, @RequestParam String env, @RequestParam String serverId) throws Exception {
        log.info("login!");
        UserProtos.LoginReq.Builder loginReq = UserProtos.LoginReq.newBuilder();
        loginReq.setAccount(account);
        loginReq.setPwd(DigestUtils.md5DigestAsHex("123456".getBytes(StandardCharsets.UTF_8)));
        loginReq.setMac("");
        loginReq.setIdfa("");
        loginReq.setGameTypeId(5);
        loginReq.setSystem("HarmonyOS");
        byte[] bytes = OkhttpUtils.request("http://192.168.1.114:8087/userC/login/", loginReq.build().toByteArray());
        UserProtos.UserRes res = UserProtos.UserRes.parseFrom(bytes);
        log.info("1:{}", res);
        if (ErrorCode.SUCCESS.code() != res.getRet()) {
            UserProtos.RegisterReq.Builder registerReq = UserProtos.RegisterReq.newBuilder();
            registerReq.setAccount(account);
            registerReq.setPwd(DigestUtils.md5DigestAsHex("123456".getBytes(StandardCharsets.UTF_8)));
            registerReq.setAndroididUuid(DigestUtils.md5DigestAsHex(account.getBytes(StandardCharsets.UTF_8)));
            registerReq.setChannel("SLI");
            registerReq.setChannelInfo("LI_ANDROID");
            registerReq.setClientSystemVersion("");
            registerReq.setClientVersion("1000000");
            registerReq.setCode("");
            registerReq.setDeviceInfoJson("");
            registerReq.setFullChannel("LI_ANDROID@@@LI");
            registerReq.setGameType(5);
            registerReq.setIdCard("");
            registerReq.setIdfa("");
            registerReq.setImeiIdfa(DigestUtils.md5DigestAsHex(account.getBytes(StandardCharsets.UTF_8)));
            registerReq.setMac("");
            registerReq.setNameCard("");
            registerReq.setPhoneModel("");
            registerReq.setPhoneName("");
            registerReq.setPhoneNumber("");
            registerReq.setPhoneSn("");
            registerReq.setSystem("HarmonyOS");
            byte[] registerByte = OkhttpUtils.request("http://192.168.1.114:8087/userC/register/", registerReq.build().toByteArray());
            res = UserProtos.UserRes.parseFrom(registerByte);
            log.info("2:{}", res);
        }
        String url = "http://" + env + "/";
        return login(res.getAccountId(), res.getToken(), url,serverId);
    }

    @GetMapping("/deleteFile")
    public int deleteFile(@RequestParam String fileName, @RequestParam String dir) throws IOException, InterruptedException {
        String pathName = filePath + dir + File.separator + fileName;
        log.info("delete file name:{}", pathName);
        Process process = Runtime.getRuntime().exec(ShellUtil.getCommand("rm -rf " + pathName));
        return process.waitFor();
    }

    @GetMapping("/getDate")
    public List<String> getDate() throws IOException, InterruptedException {
        String cmd = "ssh root@192.168.1.10 \"date +'%Y-%m-%d %H:%M:%S' && jps |grep d11-server\"";
        return executeCmd(cmd);
    }

    private List<String> executeCmd(String cmd) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(ShellUtil.getCommand(cmd));
        String line;
        List<String> strList = new ArrayList();
        InputStream is = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        while((line = reader.readLine())!= null){
            System.out.println(line);
            strList.add(line);
        }
        process.waitFor();
        is.close();
        reader.close();
        process.destroy();
        return strList;
    }

    @GetMapping("/setDate")
    public int setDate(@RequestParam String date) throws IOException, InterruptedException {
        String cmd = "ssh root@192.168.1.10 \"date -s '"+date+"'\"";
        log.info("cmd:{}",cmd);
        Process process = Runtime.getRuntime().exec(ShellUtil.getCommand(cmd));
        process.waitFor();
        return process.waitFor();
    }

    @GetMapping("/startServer")
    public int startServer() throws InterruptedException, IOException {
        String cmd = "ssh root@192.168.1.10 \"cd /data/server/d11_gs1/server/ && sh start.sh\"";
        log.info("cmd:{}",cmd);
        Process process = Runtime.getRuntime().exec(ShellUtil.getCommand(cmd));
        return process.waitFor();
    }

    @GetMapping("/stopServer")
    public int stopServer() throws InterruptedException, IOException {
        String cmd = "ssh root@192.168.1.10 \"cd /data/server/d11_gs1/server/ && sh stop.sh\"";
        log.info("cmd:{}",cmd);
        Process process = Runtime.getRuntime().exec(ShellUtil.getCommand(cmd));
        return process.waitFor();
    }

    @GetMapping("/queryTeamInfo")
    public Map<String, Object> queryTeamInfo(@RequestParam String teamName){
        String evnTag = configDao.getEvnTag();
        JdbcTemplate jdbcTemplate = JdbcUtil.getDefaultGameJdbc(evnTag);
        Map<String, Object> teamInfo = teamDao.getTeamInfo(jdbcTemplate, teamName);
        if (teamInfo == null) {
            return null;
        }
        String teamId = (String) teamInfo.get("team_id");
        Map<String, Object> energyInfo = teamDao.getTeamEnergy(jdbcTemplate, teamId);
        if (energyInfo != null) {
            teamInfo.putAll(energyInfo);
        }
        return teamInfo;
    }

    @GetMapping("/updateTeamInfo")
    public int updateTeamInfo(@RequestParam String teamId,@RequestParam String teamName,@RequestParam Integer luckyNum,
                              @RequestParam Integer max,@RequestParam Integer cur){
        String evnTag = configDao.getEvnTag();
        JdbcTemplate jdbcTemplate = JdbcUtil.getDefaultGameJdbc(evnTag);
        teamDao.updateTeamInfo(jdbcTemplate, teamId, teamName, luckyNum);
        teamDao.updateTeamEnergy(jdbcTemplate, teamId, max, cur);
        return 0;
    }

    public static String send(String url, String req, String res, String jsonStr, String env) throws Exception {
        RequestComponent requestComponent;
        if (!StringUtils.isEmpty(req)) {
            req = req.replace(".", "$");
            String reqStr = Constant.PB_PACKAGE_PREFIX + req;
            Class<?> buildClass1 = Class.forName(reqStr);
            Method method = RequestComponent.getMethod(buildClass1, "newBuilder", null);
            Class<?> buildClass2 = Class.forName(reqStr + "$Builder");
            Message.Builder messageOrBuilder = (Message.Builder) method.invoke(buildClass2);
            JsonFormat.merge(jsonStr, messageOrBuilder);
            requestComponent = new RequestComponent(messageOrBuilder.build(), env);
        } else {
            requestComponent = new RequestComponent(null, env);
        }
        res = res.replace(".", "$");
        Class<?> resClass = Class.forName(Constant.PB_PACKAGE_PREFIX + res);
        return requestComponent.exec(url, resClass);
    }

    public static String login(String account, String token, String env,String serverId) throws Exception {
        String strPrefix = "net.galasports.demo.protocol.";
        Class<?> loginReqClass = Class.forName(strPrefix + "LoginProtos$LoginReq");
        Class<?> loginReqBuildClass = Class.forName(strPrefix + "LoginProtos$LoginReq$Builder");
        Class<?> loginResClass = Class.forName(strPrefix + "LoginProtos$LoginRes");
        Method method = RequestComponent.getMethod(loginReqClass, "newBuilder", null);
        Message.Builder messageOrBuilder = (Message.Builder) method.invoke(loginReqBuildClass);
        String jsonFormat = "{channel:\"SLI\",account_id:\"" + account + "\",token:\"" + token + "\",server_id:\""+serverId+"\"}";
        JsonFormat.merge(jsonFormat, messageOrBuilder);
        RequestComponent requestComponent = new RequestComponent(messageOrBuilder.build(), env);
        return requestComponent.exec("loginC/login", loginResClass);
    }

    @GetMapping("/getLogDir")
    public List<String> getLog() throws Exception {
        File configFile = new File(filePath + Constant.FRONT_LOG + File.separator);
        List<String> dirNames = new ArrayList<>();
        File[] configFiles = configFile.listFiles();
        if (configFiles != null) {
            for (File f : configFiles) {
                boolean directory = f.isDirectory();
                if (directory) {
                    String name = f.getName();
                    dirNames.add(name);
                }
            }
        }
        return dirNames;
    }

    @GetMapping("/getLogFile")
    public List<String> getLogFile() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dir = sdf.format(new Date());
        String path = "/data/server/client_log/";
        String os = System.getProperty("os.name");
        if (os.toLowerCase().startsWith("win")) {
            path = "E:\\project\\log\\";
        }
        File configFile = new File(path + dir + File.separator);
        boolean exists = configFile.exists();
        if (!exists) {
            boolean mkdir = configFile.mkdir();
            if (!mkdir) {
                log.error("create log file fail!");
                return new ArrayList<>();
            }
        }
        List<String> fileNames = new ArrayList<>();
        File[] configFiles = configFile.listFiles();
        if (configFiles != null) {
            for (File f : configFiles) {
                boolean directory = f.isFile();
                if (directory) {
                    String name = f.getName();
                    fileNames.add(name);
                }
            }
        }
        return fileNames;
    }

    @RequestMapping("downFile")
    public void downSql(@RequestParam String fileName, HttpServletResponse res) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dir = sdf.format(new Date());
        String path = "/data/server/client_log/";
        String os = System.getProperty("os.name");
        if (os.toLowerCase().startsWith("win")) {
            path = "E:\\project\\log\\";
        }
        String configFile = path + dir + File.separator + fileName;
        File file = new File(configFile);
        FileUtil.downloadFile(res, file, file.getName());
    }

}
