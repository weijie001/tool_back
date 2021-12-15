package com.tool.init;

import com.tool.bean.Config;
import com.tool.dao.ConfigDao;
import com.tool.jdbc.JdbcInfo;
import com.tool.jdbc.JdbcInfoDao;
import com.tool.jdbc.JdbcManage;
import com.tool.manage.ConfigManage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Component
public class ClassInit implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(ClassInit.class);
    private static final String CONFIG_FILE = "config";

    @Autowired
    protected JdbcInfoDao jdbcInfoDAO;

    @Autowired
    protected ConfigDao configDao;
    @Value("${file.path}")
    public  String filePath;

    @Override
    public void afterPropertiesSet() throws IOException {
        List<JdbcInfo> jdbcInfos = jdbcInfoDAO.getGameAllJdbcInfo();
        List<Config> allConfig = configDao.getAllConfig();
        JdbcManage.initJdbcInfo(jdbcInfos);
        ConfigManage.initConfig(allConfig);
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
            return;
        }
        String pathName = filePath+CONFIG_FILE+File.separator+fileName;
        log.info("jar fileName {}",pathName);
        loadJar(pathName);

    }

    public  void loadJar(String jarPath) throws IOException {
        File jarFile = new File(jarPath);
        JarFile jarFile1 = new JarFile(jarPath);

        for (Enumeration<JarEntry> e = jarFile1.entries(); e.hasMoreElements(); ) {
            JarEntry entry = e.nextElement();
            String name = entry.getName();
            if (name.endsWith(".proto")) {
                InputStream inputStream = jarFile1.getInputStream(entry);
                ProtoManager.byName.put(entry.getName(), inputStream);
            }
        }
        //文件存在
        if (jarFile.exists() == false) {
            log.info("jar file not found.");
            return;
        }

        //从URLClassLoader类加载器中获取类的addURL方法
        Method method = null;
        try {
            method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        } catch (NoSuchMethodException | SecurityException e1) {
            e1.printStackTrace();
        }
        // 获取方法的访问权限
        boolean accessible = method.isAccessible();
        try {
            //修改访问权限为可写
            if (accessible == false) {
                method.setAccessible(true);
            }
            // 获取系统类加载器
            URLClassLoader classLoader = (URLClassLoader) this.getClass().getClassLoader();
            //获取jar文件的url路径
            URL url = jarFile.toURI().toURL();
            //jar路径加入到系统url路径里
            method.invoke(classLoader, url);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            method.setAccessible(accessible);
        }
    }


}
