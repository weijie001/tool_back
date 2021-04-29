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


    public int add(String account,String token,String env) {
        String sql = "insert into t_test_account(account,token,env) values(?,?,?)";
        //int update = jdbcTemplate.update(sql, account, token, env);
        KeyHolder holder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, account);
            ps.setString(2, token);
            ps.setString(3, env);
            return ps;
        }, holder);
        return Objects.requireNonNull(holder.getKey()).intValue();
    }

    public void delete(int id) {
        String sql = "delete from t_test_account where id=?";
        int update = jdbcTemplate.update(sql, id);
    }
}
