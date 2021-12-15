package com.tool.controller;

import com.tool.bean.PageList;
import com.tool.dao.CommonDao;
import com.tool.dao.ConfigDao;
import com.tool.dao.TeamDao;
import com.tool.util.JdbcUtil;
import com.tool.util.TableUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/playerC")
public class PlayerController {

    @Resource
    private CommonDao commonDao;
    @Resource
    private TeamDao teamDao;
    @Autowired
    ConfigDao configDao;
    @RequestMapping("/getPlayers")
    public PageList getPlayers(@RequestParam String playerId, @RequestParam String rarity,
                               @RequestParam String page,@RequestParam String pageRow) {
        String evnTag = configDao.getEvnTag();
        JdbcTemplate jdbcTemplate = JdbcUtil.getDefaultDataJdbc(evnTag);
        String sql = "select * from t_player where 1=1 ";
        if (!StringUtils.isEmpty(playerId)) {
            sql += " and player_id = "+playerId;
        }
        if (!StringUtils.isEmpty(rarity)) {
            sql += " and rarity = "+rarity;
        }
        return   commonDao.queryPage(jdbcTemplate, sql,Integer.parseInt(page),Integer.parseInt(pageRow));
    }

    @RequestMapping("/addPlayers")
    public boolean addPlayers(@RequestParam List<String> playerIds, String teamId) {
        String evnTag = configDao.getEvnTag();
        JdbcTemplate dataJdbcTemplate = JdbcUtil.getDefaultDataJdbc(evnTag);
        JdbcTemplate gameJdbcTemplate = JdbcUtil.getDefaultGameJdbc(evnTag);

        Map<String, Object> teamInfo = teamDao.getTeam(gameJdbcTemplate, teamId);
        if (teamInfo == null) {
            return false;
        }
        String sql = "select * from t_player where player_id in ("+String.join(",",playerIds)+")";
        List<Map<String, Object>> players = commonDao.tableData(dataJdbcTemplate,sql);
        String teamNumSql = "select * from t_team_player_num wher where team_id = '"+teamId+"'";
        Map<String, Object> teamNumMap = commonDao.queryOne(gameJdbcTemplate,teamNumSql);
        Long num = (Long)teamNumMap.get("num");
        String tableName = TableUtil.getTableByTeamId("t_team_player_base", 10, teamId);
        String tableName2 = TableUtil.getTableByTeamId("t_team_player_cultivate", 10, teamId);
        StringBuilder sqlPrefix1 = new StringBuilder("insert into " + tableName + "(`team_id`,`player_num`,`model`,`player_id`,`shirt_number`,`acess`,`create_time`) values('"+teamId+"',");
        StringBuilder sqlPrefix2 = new StringBuilder("insert into " + tableName2 + "(`team_id`,`player_num`,`player_id`,`skill_level`) values('"+teamId+"',");
        Date now = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowStr = simpleDateFormat.format(now);
        for (Map<String, Object> player : players) {
            StringBuilder sqlMid1 = new StringBuilder();
            StringBuilder sqlMid2 = new StringBuilder();
            sqlMid1.append("'").append(num).append("',");
            sqlMid1.append("'").append(player.get("model")).append("',");
            sqlMid1.append("'").append(player.get("player_id")).append("',");
            sqlMid1.append("'").append(player.get("shirt_number")).append("',");
            sqlMid1.append("'").append("1").append("',");
            sqlMid1.append("'").append(nowStr).append("',");
            sqlMid2.append("'").append(num).append("',");
            sqlMid2.append("'").append(player.get("player_id")).append("',");
            Integer skillLevel = (Integer)player.get("skill_id");
            sqlMid2.append("'").append(skillLevel > 0 ? 1 : 0).append("',");
            num++;
            String sqlConcat1 = sqlMid1.toString().substring(0, sqlMid1.toString().length() - 1);
            String sqlConcat2 = sqlMid2.toString().substring(0, sqlMid2.toString().length() - 1);

            String teamPlayerSql = sqlPrefix1.toString() + sqlConcat1 + ")";
            commonDao.execute(gameJdbcTemplate, teamPlayerSql);
            String cultivateSql = sqlPrefix2.toString() + sqlConcat2 + ")";
            commonDao.execute(gameJdbcTemplate, cultivateSql);
        }
        String updateSql = "update t_team_player_num set num = " + num + " where team_id = '" + teamId + "'";
        commonDao.execute(gameJdbcTemplate,updateSql);
        return true;
    }
}
