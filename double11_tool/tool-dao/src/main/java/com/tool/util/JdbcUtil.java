package com.tool.util;

import com.tool.jdbc.JdbcInfo;
import com.tool.jdbc.JdbcManage;
import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcUtil {
    public static  JdbcTemplate getDefaultDataJdbc(String envTag) {
        return JdbcManage.getTemplateBy(envTag, JdbcInfo.MARK_DATA);
    }

    public static  JdbcTemplate getDefaultGameJdbc(String envTag) {
        return JdbcManage.getTemplateBy(envTag, JdbcInfo.MARK_GAME);
    }
}
