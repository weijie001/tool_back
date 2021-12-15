package com.tool.dao;

import com.tool.bean.JdbcInfo;
import com.tool.mapper.JdbcInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JdbcInfoDao {
    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    private JdbcInfoMapper mapper;
    public List<JdbcInfo> getGameAllJdbcInfo() {
        String sql = "select * from `t_jdbc_info`";
        return jdbcTemplate.query(sql, mapper);
    }
}
