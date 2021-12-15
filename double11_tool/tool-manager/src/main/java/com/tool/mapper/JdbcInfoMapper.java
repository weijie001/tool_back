package com.tool.mapper;


import com.tool.bean.JdbcInfo;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class JdbcInfoMapper implements RowMapper<JdbcInfo> {


    @Override
    public JdbcInfo mapRow(ResultSet resultSet, int i) throws SQLException {
        JdbcInfo bean = new JdbcInfo();
        bean.setServerId(resultSet.getString("server_id"));
        bean.setServerName(resultSet.getString("server_name"));
        bean.setJdbcUrl(resultSet.getString("jdbc_url"));
        bean.setJdbcAccountId(resultSet.getString("jdbc_account_id"));
        bean.setJdbcPwd(resultSet.getString("jdbc_pwd"));
        bean.setMark(resultSet.getString("mark").trim());
        bean.setChannelMark(resultSet.getString("channel_mark").trim());
        bean.setDefaultChannelData(resultSet.getInt("default_data"));
        return bean;
    }
}
