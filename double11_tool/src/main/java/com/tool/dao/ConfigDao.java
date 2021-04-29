package com.tool.dao;

import com.tool.bean.Config;
import com.tool.mapper.ConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ConfigDao {
    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected ConfigMapper mapper;
    public List<Config> getAllConfig() {
        String sql = "select * from `t_config`";
        return jdbcTemplate.query(sql, mapper);
    }
}
