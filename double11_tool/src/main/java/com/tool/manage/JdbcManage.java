package com.tool.manage;

import com.tool.bean.JdbcInfo;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.ObjectUtils;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JdbcManage {
    private static Map<String, JdbcTemplate> dataTemplateMap = new HashMap<>();
    private static Map<String, JdbcTemplate> gameTemplateMap = new HashMap<>();
    private static Map<String, JdbcInfo> gameJdbcMap = new HashMap<>();

    public static void initJdbcInfo(List<JdbcInfo> jdbcInfos) {
        for (JdbcInfo bean : jdbcInfos) {
            // 创建连接池和模板
            BasicDataSource dataSource = bean.createJdbcTemplate();
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            if (JdbcInfo.MARK_DATA.equals(bean.getMark())) {
                JdbcTemplate originalDataJdbcTemplate = dataTemplateMap.get(bean.getChannelMark());
                // 先放入新数据，再关闭之前的 JdbcTemplate
                dataTemplateMap.put(bean.getChannelMark(), jdbcTemplate);
                closeDataSource(originalDataJdbcTemplate);
            } else if (JdbcInfo.MARK_GAME.equals(bean.getMark())) {
                JdbcTemplate originalGameJdbcTemplate = gameTemplateMap.get(bean.getServerId());
                // 先放入新数据，再关闭之前的 JdbcTemplate
                gameTemplateMap.put(bean.getServerId(), jdbcTemplate);
                closeDataSource(originalGameJdbcTemplate);
            }
        }
        gameJdbcMap = jdbcInfos.stream().filter(f -> f.getMark().equals(JdbcInfo.MARK_GAME)).collect(Collectors.toMap(JdbcInfo::getServerId, x -> x));

    }

    private static void closeDataSource(JdbcTemplate jdbcTemplate) {
        if (jdbcTemplate != null) {
            BasicDataSource dataSource = (BasicDataSource) jdbcTemplate.getDataSource();
            if (dataSource != null) {
                try {
                    dataSource.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
    }

    public static JdbcTemplate getTemplateBy(String serverId, String mark) {
        JdbcTemplate jdbcTemplate = null;
        if (JdbcInfo.MARK_GAME.equals(mark)) {
            jdbcTemplate = gameTemplateMap.get(serverId);
        } else if (JdbcInfo.MARK_DATA.equals(mark)) {
            JdbcInfo jdbcInfo = gameJdbcMap.get(serverId);
            if (!ObjectUtils.isEmpty(jdbcInfo)) {
                jdbcTemplate = dataTemplateMap.get(jdbcInfo.getChannelMark());
            }
        } else {
            System.out.println("==============================");
            //log.error("=====> errorSeverId :{},mark:{}", serverId, mark);
            //throw new CustomerException(ErrorCode.REQUEST_WRONG_PARAM);
        }
        return jdbcTemplate;
    }
}
