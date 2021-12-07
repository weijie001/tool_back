package com.tool.controller;

import com.tool.bean.ResponseResult;
import com.tool.compare.DatabaseComparator;
import com.tool.compare.DatabaseStructureLoader;
import com.tool.compare.context.*;
import com.tool.compare.model.Database;
import com.tool.util.JdbcUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@RequestMapping("dbC")
@RestController
public class DbCompareController extends BaseController{

    @Autowired
    private DatabaseStructureLoader databaseStructureLoader;

    @RequestMapping("tableSql")
    public ResponseResult<Boolean> tableSql(@RequestParam Integer id) {
        List<MatchedTableItem> matchedTableItems = SqlManager.getMatchedTableItems();
        JdbcTemplate jdbcTemplate = JdbcUtil.getDefaultDataJdbc("test");
        matchedTableItems.stream().filter(t->t.getId() == id).findFirst().ifPresent(matchedTableItem -> {
            String sql = matchedTableItem.getSql();
            if (!StringUtils.isEmpty(sql)) {
                jdbcTemplate.execute(sql);
            }
            List<MatchedColumnItem> columns = matchedTableItem.getColumns();
            for (MatchedColumnItem matchedColumnItem : columns) {
                sql = matchedColumnItem.getSql();
                if (!StringUtils.isEmpty(sql)) {
                    jdbcTemplate.execute(sql);
                }
            }
            List<MatchedIndexItem> indexs = matchedTableItem.getIndexs();
            for (MatchedIndexItem matchedIndexItem : indexs) {
                sql = matchedIndexItem.getSql();
                if (!StringUtils.isEmpty(sql)) {
                    jdbcTemplate.execute(sql);
                }
            }
        });
        return new ResponseResult<>(0, true, null);
    }
    @RequestMapping("execSql")
    public ResponseResult<Boolean> execSql(@RequestParam String tableName) {
        List<CompareData> compareData = SqlManager.getCompareData();
        JdbcTemplate jdbcTemplate = JdbcUtil.getDefaultDataJdbc("test");
        AtomicBoolean present = new AtomicBoolean(false);
        compareData.stream().filter(t -> t.getTableName().equals(tableName)).findFirst().ifPresent(compareData1 -> {
            List<String> execSqls = compareData1.getExecSqls();
            present.set(true);
            for (String sql : execSqls) {
                jdbcTemplate.execute(sql);
            }
        });
        return new ResponseResult<>(0, present.get(), null);
    }
    @RequestMapping("compare2")
    public ResponseResult<List<CompareData>> compare2(@RequestParam("userId") String userId){
        JdbcTemplate leftJdbcTemplate = JdbcUtil.getDefaultDataJdbc("dev");
        List<CompareData> compareData = databaseStructureLoader.loadColumns(leftJdbcTemplate, "d11_data_dev",userId);
        SqlManager.setCompareData(compareData);
        return new ResponseResult<>(0, compareData, null);
    }

    @RequestMapping("compare")
    public ResponseResult<List<MatchedTableItem>> compare(){
        long start = System.currentTimeMillis();
        DatabaseComparator comparator = new DatabaseComparator();
        JdbcTemplate leftJdbcTemplate = JdbcUtil.getDefaultDataJdbc("dev");
        JdbcTemplate rightJdbcTemplate = JdbcUtil.getDefaultDataJdbc("test");
        //Database left_db = databaseStructureLoader.loadDatabase(new DBConnectionConfig("mysql", "192.168.1.201", "3309", "root", "wckj123!@#", "d11_data_dev"));
        //Database rigth_db = databaseStructureLoader.loadDatabase(new DBConnectionConfig("mysql", "192.168.1.201", "3309", "root", "wckj123!@#", "d11_data_test"));
        Database left_db = databaseStructureLoader.loadDatabase(leftJdbcTemplate,"d11_data_dev");
        Database rigth_db = databaseStructureLoader.loadDatabase(rightJdbcTemplate,"d11_data_test");

        //log.info(String.format("加载表结构数据成功.... 耗时:%s ms",System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        MatchedDatabaseContext context = comparator.compare(left_db, rigth_db);
        List<MatchedTableItem> tableItems = context.getTables();
        List<MatchedTableItem> matchedTableItems = new ArrayList<>();
        List<MatchedTableItem> rightTables = tableItems.stream().filter(t -> t.getResult() == CompareResult.RIGHT_NOT_EXIST).collect(Collectors.toList());
        List<MatchedTableItem> leftTables = tableItems.stream().filter(t -> t.getResult() == CompareResult.LEFT_NOT_EXIST).collect(Collectors.toList());
        List<MatchedTableItem> tableNames = tableItems.stream().filter(t -> t.getResult() == CompareResult.NOT_EQUAL).collect(Collectors.toList());
        matchedTableItems.addAll(leftTables);
        matchedTableItems.addAll(rightTables);
        matchedTableItems.addAll(tableNames);
        String result = "no data";
        for (MatchedTableItem matchedTableItem : tableNames) {
            String tableName = matchedTableItem.getLeft().getName();
            String sql = "show create table " + tableName;
            List<Map<String, Object>> maps = leftJdbcTemplate.queryForList(sql);
            //log.info("==================");
            result = (String)maps.get(0).get("Create Table");
            break;
        }
        //log.info(String.format("比对数据库结构成功! 耗时:%s ms",System.currentTimeMillis() - start));
        SqlManager.setMatchedTableItems(matchedTableItems);
        return new ResponseResult<>(0,matchedTableItems,null);
    }


}
