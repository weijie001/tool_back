package com.tool.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.*;

public class FileToolUtil {
    private static final Logger log = LoggerFactory.getLogger(FileToolUtil.class);
    private static final String PROTO_FILE = "proto";
    public static String getContent(String str,String filePath) throws IOException {
        String req = str.split("\\.")[1];
        String path = filePath+PROTO_FILE+File.separator;
        log.info("proto file path:{}",path);
        File file = new File(path);
        File[] files = file.listFiles();
        File findFile = null;
        if (files != null) {
            for (File f : files) {
                boolean isFind = getFile(f, req);
                if (isFind) {
                    findFile = f;
                    break;
                }
            }
        }
        String containValid = null;
        if (findFile !=null) {
            log.info("find proto file:{}",findFile.getName());
            String param = getParam(findFile, req);
            if (param != null) {
                containValid = isContainValid(param);
            }
        }
        log.info("input param:{}",containValid);
        return containValid;
    }

    private static String isContainValid(String param) {
        String result;
        if (param.contains(",}")) {
            param = param.replaceAll(",}", "}");
            result = isContainValid(param);
        } else {
            return param;
        }
        return result;
    }

    public static String getParam(File file,String req) {
        InputStream input = null;
        BufferedReader reader = null;
        try {
            input = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(input));
            String tempString;
            boolean isFind =false;
            String str = "";
            while ((tempString = reader.readLine()) != null) {
                if (isFind) {
                    if(tempString.contains("}")){
                        str = str.substring(0, str.length() - 1);
                        str =str + "}";
                        isFind = false;
                    }else{
                        String[] split = tempString.split("\\s+");
                        String s1 = split[1];
                        String s2 = split[2];
                        String s3 = split[3];
                        if(s1.equals("sint32")||s1.equals("int32")){
                            str =str+ "\"" + s2 + "\"" + ":" + "1,";
                        }else if(s1.equals("string")){
                            str =str+ "\"" + s2 + "\"" + ":" + "\"string\",";
                        }else if(s1.equals("bool")){
                            str =str+  "\"" + s2 + "\"" + ":" + "true,";
                        }else if(s1.equals("repeated")){
                            if(s2.equals("sint32")){
                                str =str+  "\"" + s3 + "\"" + ":" + "[1,2],";
                            }else{
                                str = str + "\"" + s3 + "\"" + ":" + "";
                                String r = getParam(file, s2);
                                if (StringUtils.isEmpty(r)) {
                                    r = "{},";
                                }
                                str = str + r+",";
                            }
                        }
                    }

                }
                if (tempString.contains(req) && tempString.contains("message")) {
                    isFind = true;
                    str =str + "{";
                }
            }
            return str;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
        return null;
    }
    public static boolean  getFile(File file, String req) {
        InputStream input = null;
        BufferedReader reader = null;
        try {
            input = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(input));
            String tempString;
            boolean isFind=false;
            while ((tempString = reader.readLine()) != null) {
                if (tempString.contains(req) && tempString.contains("message")) {
                    isFind = true;
                    break;
                }
            }
            return isFind;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
        return false;
    }
}
