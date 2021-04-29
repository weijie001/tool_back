package com.tool.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public class TeamDao {

    public Map<String, Object> getTeamInfo(JdbcTemplate template, String teamId) {
        String sql = "select * from t_team where team_id = ?";
        List<Map<String, Object>> result = template.queryForList(sql, teamId);
        return result.size()>0?result.get(0):null;
    }
}
