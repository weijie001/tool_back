package com.tool.bean;


import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.SQLException;


public class JdbcInfo{

    //game库标记
    public static final String MARK_GAME = "game";
    //data库标记
    public static final String MARK_DATA = "data";


    public static final String MYSQL = "jdbc:mysql://";
    public static final String MYSQL_CODING	= "?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8&rewriteBatchedStatements=true";
    public static final String DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
    public static final int TIME_BETWEEN_EVICTION_RUNS_MILLIS = 3600000;
    public static final int MIN_EVICTABLE_IDLE_TIME_MILLIS = 18000000;
    public static final String VALIDATION_QUERY = "select 1";

    /** 服务器id*/
    private String serverId;
    /** 服务器名称*/
    private String serverName;
    /** 数据库地址*/
    private String jdbcUrl;
    /** 数据库账号*/
    private String jdbcAccountId;
    /** 数据库密码*/
    private String jdbcPwd;
    /** 标记*/
    private String mark;
    /**大渠道标识，（官网 应用宝 混服）**/
    private String channelMark;
    /**基础数据来源渠道**/
    private int defaultChannelData;


    public String getServerId() {
        return this.serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getServerName() {
        return this.serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getJdbcUrl() {
        return this.jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getJdbcAccountId() {
        return this.jdbcAccountId;
    }

    public void setJdbcAccountId(String jdbcAccountId) {
        this.jdbcAccountId = jdbcAccountId;
    }

    public String getJdbcPwd() {
        return this.jdbcPwd;
    }

    public void setJdbcPwd(String jdbcPwd) {
        this.jdbcPwd = jdbcPwd;
    }

    public String getMark() {
        return this.mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public String getChannelMark() {
        return channelMark;
    }

    public void setChannelMark(String channelMark) {
        this.channelMark = channelMark;
    }

    public int getDefaultChannelData() {
        return defaultChannelData;
    }

    public void setDefaultChannelData(int defaultChannelData) {
        this.defaultChannelData = defaultChannelData;
    }

    @Override
    public String toString() {
        return "JdbcInfo{" +
                "serverId=" + serverId +
                ",serverName=" + serverName +
                ",jdbcUrl=" + jdbcUrl +
                ",jdbcAccountId=" + jdbcAccountId +
                ",jdbcPwd=" + jdbcPwd +
                ",mark=" + mark +
                "}";
    }

    /**
     * 返回数据源
     * @return
     */
    public BasicDataSource createJdbcTemplate(){
        BasicDataSource dateSource = new BasicDataSource();
        dateSource.setDriverClassName(DRIVER_CLASS_NAME);
        dateSource.setUrl(jdbcUrl);
        dateSource.setUsername(jdbcAccountId);
        dateSource.setPassword(jdbcPwd);
        dateSource.setTimeBetweenEvictionRunsMillis(TIME_BETWEEN_EVICTION_RUNS_MILLIS);
        dateSource.setMinEvictableIdleTimeMillis(MIN_EVICTABLE_IDLE_TIME_MILLIS);
        dateSource.setTestWhileIdle(true);
        dateSource.setTestOnBorrow(true);
        dateSource.setTestOnCreate(true);
        dateSource.setTestOnReturn(true);
        dateSource.setValidationQuery(VALIDATION_QUERY);
        dateSource.setInitialSize(1);
        dateSource.setMinIdle(1);
        dateSource.setMaxWaitMillis(3000);
        dateSource.setMaxTotal(10);
        dateSource.setMaxIdle(10);
        try {
            dateSource.getLogWriter();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return dateSource;
    }


}
