package com.tool.util;

import com.tool.controller.ConfigController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FileUtil {
    private static final Logger log = LoggerFactory.getLogger(FileUtil.class);
    public static boolean upload(MultipartFile file, String dir, String filePath) {
        String pathDir = filePath + dir + File.separator;
        String fileName = file.getOriginalFilename();
        String pathName = pathDir + File.separator + fileName;
        log.info("upload file,path:{}",pathName);
        Path path = new File(pathName).toPath();
        InputStream inputStream = null;
        boolean isSuccess = true;
        try {
            inputStream = file.getInputStream();
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            isSuccess = false;
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    isSuccess = false;
                    ex.printStackTrace();
                }
            }
        }
        return isSuccess;
    }

    public  static void downloadFile(HttpServletResponse response, File file, String newFileName) {
        try {
            response.setHeader("Content-Disposition", "attachment; filename=" + new String(newFileName.getBytes("ISO-8859-1"), "UTF-8"));
            BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream());
            InputStream is = new FileInputStream(file.getAbsolutePath());
            BufferedInputStream bis = new BufferedInputStream(is);
            int length = 0;
            byte[] temp = new byte[1 * 1024 * 10];
            while ((length = bis.read(temp)) != -1) {
                bos.write(temp, 0, length);
            }
            bos.flush();
            bis.close();
            bos.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
