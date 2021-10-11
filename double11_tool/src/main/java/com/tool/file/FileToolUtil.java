package com.tool.file;

import com.tool.constant.Constant;
import com.tool.init.ProtoManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 解析pb文件
 */
public class FileToolUtil {

    private static final Logger log = LoggerFactory.getLogger(FileToolUtil.class);

    /**
     * 根据pb名称转json格式字符串
     * @param str
     * @param filePath
     * @return
     */
    public static String getContent(String str,String filePath) throws IOException {


        String path = filePath+"config"+File.separator;
        log.info("path:{}",path);
        File file = new File(path);

        String fileName = null;
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.getName().endsWith("jar")) {
                    fileName =f.getName();
                    break;
                }
            }
        }
        if (StringUtils.isEmpty(fileName)) {
            log.info("jar file not exist!");
            return null;
        }
        String jarPath = filePath+Constant.CONFIG+File.separator+fileName;
        String req = str.split("\\.")[1];
        JarEntry findFile = null;
        JarFile jarFile1 = new JarFile(jarPath);
        for (Enumeration<JarEntry> e = jarFile1.entries(); e.hasMoreElements(); ) {
            JarEntry entry = e.nextElement();
            String name = entry.getName();
            if (name.endsWith(".proto")) {
                boolean isFind = getFile(entry, req,jarFile1);
                if (isFind) {
                    findFile = entry;
                    break;
                }
            }
        }

        String containValid = null;
        if (findFile !=null) {
            log.info("find proto file:{}",findFile.getName());
            String param = getParam(findFile, req,jarFile1);
            if (param != null) {
                containValid = isContainValid(param,jarFile1);
            }
        }
        log.info("input param:{}",containValid);
        return containValid;
    }

    private static String isContainValid(String param,JarFile jarFile1) {
        String result;
        if (param.contains(",}")) {
            param = param.replaceAll(",}", "}");
            result = isContainValid(param,jarFile1);
        } else {
            return param;
        }
        return result;
    }

    public static String getParam(JarEntry file,String req,JarFile jarFile) {
        InputStream input = null;
        BufferedReader reader = null;
        try {
            input = jarFile.getInputStream(file);
            reader = new BufferedReader(new InputStreamReader(input));
            String tempString;
            boolean isFind =false;
            StringBuilder str = new StringBuilder();
            while ((tempString = reader.readLine()) != null) {
                if (isFind) {
                    if(tempString.contains("}")){
                        str = new StringBuilder(str.substring(0, str.length() - 1));
                        str.append("}");
                        isFind = false;
                    }else{
                        String[] split = tempString.split("\\s+");
                        String s1 = split[1];
                        String s2 = split[2];
                        String s3 = split[3];
                        switch (s1) {
                            case "sint32":
                            case "int32":
                                str.append("\"").append(s2).append("\"").append(":").append("1,");
                                break;
                            case "string":
                                str.append("\"").append(s2).append("\"").append(":").append("\"string\",");
                                break;
                            case "bool":
                                str.append("\"").append(s2).append("\"").append(":").append("true,");
                                break;
                            case "repeated":
                                if (s2.equals("sint32")) {
                                    str.append("\"").append(s3).append("\"").append(":").append("[1,2],");
                                } else {
                                    str.append("\"").append(s3).append("\"").append(":");
                                    String r = getParam(file, s2,jarFile);
                                    if (StringUtils.isEmpty(r)) {
                                        r = "{},";
                                    }
                                    str.append(r).append(",");
                                }
                                break;
                        }
                    }

                }
                if (tempString.toUpperCase().startsWith(("message "+req).toUpperCase()) && tempString.contains("message")) {
                    isFind = true;
                    str.append("{");
                }
            }
            return str.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            closeStream(input, reader);
        }
        return null;
    }

    private static void closeStream(InputStream input, BufferedReader reader) {
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

    public static boolean  getFile(JarEntry file, String req,JarFile jarFile) {
        InputStream input = null;
        BufferedReader reader = null;
        try {
            input = jarFile.getInputStream(file);
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
            closeStream(input, reader);
        }
        return false;
    }
}
