package com.tool.util;

import com.tool.bean.JdbcInfo;
import com.tool.constant.Constant;
import com.tool.manage.ConfigManage;
import com.tool.manage.JdbcManage;
import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcUtil {
    public static  JdbcTemplate getDefaultDataJdbc() {
        return JdbcManage.getTemplateBy(ConfigManage.getConfig(Constant.DEFAULT_DATA_BASE).getConfigValue(), JdbcInfo.MARK_DATA);
    }

    public static  JdbcTemplate getDefaultGameJdbc() {
        return JdbcManage.getTemplateBy(ConfigManage.getConfig(Constant.DEFAULT_GAME_BASE).getConfigValue(), JdbcInfo.MARK_GAME);
    }
}
