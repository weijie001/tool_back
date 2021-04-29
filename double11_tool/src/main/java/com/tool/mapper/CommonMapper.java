package com.tool.mapper;

import com.tool.bean.CommonBean;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
@Repository
public class CommonMapper implements RowMapper<CommonBean> {

    @Override
    public CommonBean mapRow(ResultSet resultSet, int i) throws SQLException {
        CommonBean bean = new CommonBean();
        bean.setColumnName(resultSet.getString("column_name"));
        bean.setDataType(resultSet.getString("data_type"));
        return bean;
    }

}
