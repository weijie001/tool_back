package com.tool.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;
import com.tool.bean.FileNameInfo;
import com.tool.bean.FileTypeEnum;
import com.tool.dao.TeamDao;
import com.tool.file.FileToolUtil;
import com.tool.http.RequestComponent;
import com.tool.poi.ExcelUtil;
import com.tool.util.JdbcUtil;
import com.tool.util.ShellUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/jk")
public class JkTestController {
    private static final Logger log = LoggerFactory.getLogger(JkTestController.class);

    @Value("${file.path}")
    public  String filePath;
    private static final String CONFIG_FILE = "config";
    private static final String PROTO_FILE = "proto";

    @Autowired
    private TeamDao teamDao;

    @RequestMapping("/getConfigFile")
    public List<FileNameInfo> getConfigFile() {
        log.info("get config proto file!");
        File configFile = new File(filePath+CONFIG_FILE+File.separator);
        File protoFile = new File(filePath+PROTO_FILE+File.separator);
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

    @GetMapping("/getInputParam")
    public String getParam(@RequestParam String req) throws IOException {
        log.info("get input param!");
        return FileToolUtil.getContent(req,filePath);
    }

    @GetMapping("/select")
    public JSONArray select(){
        JSONObject excelData = ExcelUtil.getExcelData(filePath);
        if (excelData == null) {
            log.info("has no data!");
            return null;
        }
        return (JSONArray) excelData.get("Sheet1");
    }

    @GetMapping("/getData")
    public String getData(@RequestParam String req, @RequestParam String res, @RequestParam String uri, @RequestParam(defaultValue = "{}") String data,
                          @RequestParam String account, @RequestParam String token, @RequestParam String env) throws Exception {
        log.info("login!");
        login(account,token,env);
        log.info("get data!");
        return send(uri, req, res, data,env);
    }

    @GetMapping("/getTeamInfo")
    public Map<String, Object> getTeamInfo(@RequestParam String teamId) throws Exception {
        JdbcTemplate devGameJdbc = JdbcUtil.getDefaultGameJdbc();
        return   teamDao.getTeamInfo(devGameJdbc, teamId);
    }
    @GetMapping("/loginValid")
    public String loginValid(@RequestParam String account, @RequestParam String token, @RequestParam String env) throws Exception {
        log.info("login!");
        return login(account,token,env);
    }

    @GetMapping("/deleteFile")
    public int deleteFile(@RequestParam String fileName,@RequestParam String dir) throws IOException, InterruptedException {
        String pathName = filePath + dir + File.separator + fileName;
        log.info("delete file name:{}",pathName);
        Process process = Runtime.getRuntime().exec(ShellUtil.getCommand("rm -rf " + pathName));
        return process.waitFor();
    }



    public static String send(String url, String req, String res, String jsonStr,String env) throws Exception {
        RequestComponent requestComponent;
        String strPrefix = "net.galasports.demo.protocol.";
        if (!StringUtils.isEmpty(req)) {
            req = req.replace(".", "$");
            String reqStr = strPrefix + req;
            Class<?> buildClass1 = Class.forName(reqStr);
            Method method = RequestComponent.getMethod(buildClass1, "newBuilder", null);
            Class<?> buildClass2 = Class.forName(reqStr + "$Builder");
            Message.Builder messageOrBuilder = (Message.Builder) method.invoke(buildClass2);
            JsonFormat.merge(jsonStr, messageOrBuilder);
            requestComponent = new RequestComponent(messageOrBuilder.build(),env);
        } else {
            requestComponent = new RequestComponent(null,env);
        }
        res = res.replace(".", "$");
        Class<?> resClass = Class.forName(strPrefix + res);
        return requestComponent.exec(url, resClass);
    }
    public static String login(String account,String token,String env) throws Exception {
        String strPrefix = "net.galasports.demo.protocol.";
        Class<?> loginReqClass = Class.forName(strPrefix + "LoginProtos$LoginReq");
        Class<?> loginReqBuildClass = Class.forName(strPrefix + "LoginProtos$LoginReq$Builder");
        Class<?> loginResClass = Class.forName(strPrefix + "LoginProtos$LoginRes");
        Method method = RequestComponent.getMethod(loginReqClass, "newBuilder", null);
        Message.Builder messageOrBuilder = (Message.Builder) method.invoke(loginReqBuildClass);
        String jsonFormat = "{channel:\"SLI\",account_id:\""+account+"\",token:\""+token+"\",server_id:\"double11_001\"}";
        JsonFormat.merge(jsonFormat, messageOrBuilder);
        RequestComponent requestComponent = new RequestComponent(messageOrBuilder.build(),env);
        return requestComponent.exec("loginC/login", loginResClass);
    }
}