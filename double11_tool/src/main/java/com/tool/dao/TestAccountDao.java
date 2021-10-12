package com.tool.dao;

import com.tool.bean.TestAccount;
import com.tool.mapper.TestAccountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

@Repository
public class TestAccountDao {
    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    private TestAccountMapper mapper;

    public List<TestAccount> getAllTestAccount() {
        String sql = "select * from `t_test_account`";
        return jdbcTemplate.query(sql, mapper);
    }


    public int add(String account,String env,String serverId,String name) {
        String sql = "insert into t_test_account(account,env,server_id,name) values(?,?,?,?)";
        KeyHolder holder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, account);
            ps.setString(2, env);
            ps.setString(3, serverId);
            ps.setString(4, name);
            return ps;
        }, holder);
        return Objects.requireNonNull(holder.getKey()).intValue();
    }

    public void delete(int id) {
        String sql = "delete from t_test_account where id=?";
        int update = jdbcTemplate.update(sql, id);
    }
}
