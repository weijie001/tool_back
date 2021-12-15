package com.tool.mapper;

import com.tool.bean.Item;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class ItemMapper implements RowMapper<Item> {
    @Override
    public Item mapRow(ResultSet resultSet, int i) throws SQLException {
        Item bean = new Item();
        bean.setItemId(resultSet.getInt("item_id"));
        bean.setName(resultSet.getString("name"));
        return bean;
    }

}
