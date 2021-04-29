package com.tool.dao;

import com.tool.bean.Item;
import com.tool.bean.ItemAward;
import com.tool.mapper.ItemMapper;
import com.tool.util.TableUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class ItemDao {

    @Autowired
    protected ItemMapper itemMapper;
    public List<Item> getItems(JdbcTemplate template,String name) {
        String sql = "select * from t_item where name like '%"+name+"%'";
        return template.query(sql, itemMapper);
    }

    public void addItems(JdbcTemplate template,String teamId, List<ItemAward> itemAwards, Timestamp now) {
        String tableName = TableUtil.getTableByTeamId("t_team_item", 10, teamId);
        String sql = "insert into " + tableName + "(`team_id`,`item_id`,`num`,`use_times`,`create_time`,`last_modify_time`) values(?,?,?,?,?,?) " +
                " on duplicate key update num = num + values(`num`)";
        int[] results = template.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ItemAward itemAward = itemAwards.get(i);
                ps.setString(1, teamId);
                ps.setInt(2, itemAward.getItemId());
                ps.setInt(3, itemAward.getNum());
                ps.setInt(4, 0);
                ps.setTimestamp(5, now);
                ps.setTimestamp(6, now);
            }
            @Override
            public int getBatchSize() {
                return itemAwards.size();
            }
        });
    }
}
