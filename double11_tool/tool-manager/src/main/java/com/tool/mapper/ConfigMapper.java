package com.tool.mapper;

import com.tool.bean.Config;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class ConfigMapper implements RowMapper<Config> {
    @Override
    public Config mapRow(ResultSet resultSet, int i) throws SQLException {
        Config bean = new Config();
        bean.setConfigKey(resultSet.getString("config_key"));
        bean.setConfigValue(resultSet.getString("config_value"));
        bean.setDesc(resultSet.getString("desc"));
        return bean;
    }
}
