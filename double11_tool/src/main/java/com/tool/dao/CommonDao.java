package com.tool.dao;

import com.tool.bean.PageList;
import com.tool.mapper.CommonMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class CommonDao {

    @Autowired
    protected CommonMapper commonMapper;
    @Autowired
    protected ConfigDao configDao;

    public List<Map<String, Object>> tableColumnsInfo(JdbcTemplate template,String tableName) {
        String dataBase = configDao.getDataBase();
        String sql = "SELECT " +
                "* " +
                "FROM " +
                "information_schema. COLUMNS " +
                "WHERE " +
                "TABLE_SCHEMA = '" + dataBase + "' AND TABLE_NAME = '" + tableName + "' order by ORDINAL_POSITION ";

        return template.queryForList(sql);
    }

    public List<Map<String, Object>> getAllTables(JdbcTemplate template) {
        String dataBase = configDao.getDataBase();
        String sql = "select `table_name` from information_schema.`TABLES` WHERE TABLE_SCHEMA = '"+dataBase+"'";
        return template.queryForList(sql);
    }

    public List<Map<String, Object>> tableData(JdbcTemplate template, String sql) {
       return   template.queryForList(sql);
    }

    public Map<String, Object> queryOne(JdbcTemplate template, String sql) {
        return template.queryForMap(sql);
    }
    public void execute(JdbcTemplate template,String sql){
        template.update(sql);
    }

    public List<Map<String, Object>> tables(JdbcTemplate template,String tableName) {
        String dataBase = configDao.getDataBase();
        String sql = "SELECT " +
                "table_name " +
                "FROM " +
                "INFORMATION_SCHEMA.TABLES wHERE " +
                "TABLE_SCHEMA = '"+dataBase+"' and table_name like '%"+tableName+"%'";
        return template.queryForList(sql);
    }

    public PageList queryPage(JdbcTemplate template, String sql,int page,int pageRow) {
        PageList pl = new PageList();
        String rowsql = "select count(*) num from (" + sql + ") t";
        Map<String, Object> rowMap = template.queryForMap(rowsql);
        Long rows = (Long) rowMap.get("num");

        long pages = 0;   //总页数
        if (rows % pageRow == 0) {
            pages = rows / pageRow;
        } else {
            pages = rows / pageRow + 1;
        }
        if (page <= 1) {
            sql += " limit 0," + pageRow;
        } else {
            sql += " limit " + ((page - 1) * pageRow) + "," + pageRow;
        }
        List<Map<String, Object>> list = template.queryForList(sql);
        pl.setPage(page);
        pl.setPages(pages);
        pl.setList(list);
        pl.setTotalRows(rows);
        return pl;

    }
}
