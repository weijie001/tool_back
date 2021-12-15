package com.tool.mapper;

import com.tool.bean.TestAccount;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class TestAccountMapper  implements RowMapper<TestAccount> {

    @Override
    public TestAccount mapRow(ResultSet resultSet, int i) throws SQLException {
        TestAccount bean = new TestAccount();
        bean.setId(resultSet.getInt("id"));
        bean.setAccount(resultSet.getString("account"));
        bean.setEnv(resultSet.getString("env"));
        bean.setServerId(resultSet.getString("server_id"));
        bean.setName(resultSet.getString("name"));
        return bean;
    }
}
