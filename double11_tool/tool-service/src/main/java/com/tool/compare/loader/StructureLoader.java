package com.tool.compare.loader;


import com.tool.compare.model.Database;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.util.List;

public interface StructureLoader {

    /**
     * 获取数据库结构
     * @param connection
     * @param databaseName
     * @return
     * @throws Exception
     */
    public Database loadDatabaseStructure(JdbcTemplate JdbcTemplate, String dataBaseName)throws Exception;


    /**
     * 通过连接获取数据库名称
     * @param connection
     * @return
     * @throws Exception
     */
    public List<String> loadDatabaseNames(Connection connection) throws Exception ;
}
