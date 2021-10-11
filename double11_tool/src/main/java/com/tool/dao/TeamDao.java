package com.tool.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public class TeamDao {

    public Map<String, Object> getTeam(JdbcTemplate template, String teamId) {
        String sql = "select * from t_team where team_id = ?";
        List<Map<String, Object>> result = template.queryForList(sql, teamId);
        return result.size()>0?result.get(0):null;
    }

    public Map<String, Object> getTeamInfo(JdbcTemplate template, String teamName) {
        String sql = "select * from t_team_info where team_name = ? or team_id = ?";
        List<Map<String, Object>> result = template.queryForList(sql, teamName,teamName);
        return result.size()>0?result.get(0):null;
    }

    public Map<String, Object> getTeamEnergy(JdbcTemplate template, String teamId) {
        String sql = "select * from t_team_energy where team_id = ?";
        List<Map<String, Object>> result = template.queryForList(sql, teamId);
        return result.size()>0?result.get(0):null;
    }

    public int updateTeamInfo(JdbcTemplate template, String teamId,String teamName,int luckyNum) {
        String sql = "update t_team_info set team_name = ?, lucky_num = ? where team_id = ?";
        return template.update(sql, teamName,luckyNum,teamId);
    }

    public int updateTeamEnergy(JdbcTemplate template, String teamId,int max,int cur) {
        String sql = "update t_team_energy set `max` = ?, `cur` = ? where team_id = ?";
        return template.update(sql, max, cur, teamId);
    }
}
