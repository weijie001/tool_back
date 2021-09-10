package com.tool.dao;

import com.tool.bean.Config;
import com.tool.constant.EnvEnum;
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

    public Config getEnv() {
        String sql = "select * from `t_config` where config_key = 'ENV'";
        List<Config> query = jdbcTemplate.query(sql, mapper);
        return query.get(0);
    }

    public int changeEnv(String tag) {
        String sql = "update t_config set config_value = ?";
        return jdbcTemplate.update(sql, tag);
    }

    public String getDataBase() {
        String configValue = getEnv().getConfigValue();
        if (EnvEnum.DEV.getEnv().equals(configValue)) {
            return EnvEnum.DEV.getData();
        }else{
            return EnvEnum.TEST.getData();
        }
    }

    public String getEvnTag() {
        return getEnv().getConfigValue();
    }

    public String getGameBase() {
        String configValue = getEnv().getConfigValue();
        if (EnvEnum.DEV.getEnv().equals(configValue)) {
            return EnvEnum.DEV.getGame();
        }else{
            return EnvEnum.TEST.getGame();
        }
    }
}
