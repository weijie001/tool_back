package com.tool.compare.config;

import lombok.Data;

@Data
public class DBConnectionConfig {

    /**
     * 数据库类型
     */
    private String type;

    public DBConnectionConfig() {
        super();
    }

    public DBConnectionConfig(String type, String host, String port, String user, String password, String database) {
        super();
        this.type = type;
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.database = database;
    }

    /**
     * ip
     */
    private String host;

    /**
     * 端口
     */
    private String port;

    /***
     * 用户
     */
    private String user;

    /**
     * 密码
     */
    private String password;


    /**
     * 数据库
     */
    private String database;
}
