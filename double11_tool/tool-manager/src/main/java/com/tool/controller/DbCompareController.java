package com.tool.controller;

import com.tool.bean.ResponseResult;
import com.tool.compare.DatabaseComparator;
import com.tool.compare.DatabaseStructureLoader;
import com.tool.compare.context.*;
import com.tool.compare.model.Database;
import com.tool.util.JdbcUtil;
import com.tool.websocket.WebSocketManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@RequestMapping("dbC")
@RestController
@Slf4j
public class DbCompareController extends BaseController{

    @Autowired
    private DatabaseStructureLoader databaseStructureLoader;

    @RequestMapping("tableSql")
    public ResponseResult<Boolean> tableSql(@RequestParam Integer id,@RequestParam("env") String env,@RequestParam("dbType") String dbType) {
        JdbcTemplate jdbcTemplate;
        if("dev".equals(env) && "game".equals(dbType)){
            jdbcTemplate = JdbcUtil.getDefaultGameJdbc("test");
        }else if("test".equals(env) && "data".equals(dbType)){
            jdbcTemplate = JdbcUtil.getDefaultGameJdbc("gs1");
        }else if("test".equals(env) && "game".equals(dbType)){
            jdbcTemplate = JdbcUtil.getDefaultGameJdbc("gs1");
        }else{
            jdbcTemplate = JdbcUtil.getDefaultDataJdbc("test");
        }
        List<MatchedTableItem> matchedTableItems = SqlManager.getMatchedTableItems();
        matchedTableItems.stream().filter(t->t.getId() == id).findFirst().ifPresent(matchedTableItem -> {
            String sql = matchedTableItem.getSql();
            if (!StringUtils.isEmpty(sql)) {
                jdbcTemplate.execute(sql);
            }
            List<MatchedColumnItem> columns = matchedTableItem.getColumns();
            if(!CollectionUtils.isEmpty(columns)){
                for (MatchedColumnItem matchedColumnItem : columns) {
                    sql = matchedColumnItem.getSql();
                    if (!StringUtils.isEmpty(sql)) {
                        jdbcTemplate.execute(sql);
                    }
                }
            }
            List<MatchedIndexItem> indexs = matchedTableItem.getIndexs();
            if(!CollectionUtils.isEmpty(indexs)){
                for (MatchedIndexItem matchedIndexItem : indexs) {
                    List<String> sqlss = matchedIndexItem.getSqls();
                    if (!CollectionUtils.isEmpty(sqlss)) {
                        for (String str : sqlss) {
                            jdbcTemplate.execute(str);
                        }
                    }
                }
            }

        });
        return new ResponseResult<>(0, true, null);
    }

    @RequestMapping("execSql")
    public ResponseResult<Boolean> execSql(@RequestParam String tableName,@RequestParam("env") String env) {
        List<CompareData> compareData = SqlManager.getCompareData();
        JdbcTemplate jdbcTemplate;
        if("test".equals(env)){
            jdbcTemplate = JdbcUtil.getDefaultDataJdbc("gs1");
        }else{
            jdbcTemplate = JdbcUtil.getDefaultDataJdbc("test");
        }
        AtomicBoolean present = new AtomicBoolean(false);
        compareData.stream().filter(t -> t.getTableName().equals(tableName)).findFirst().ifPresent(compareData1 -> {
            List<String> execSqls = compareData1.getExecSqls();
            present.set(true);
            for (String sql : execSqls) {
                String[] split = sql.split("\n");
                for (String sqlt : split) {
                    jdbcTemplate.execute(sqlt);
                }
            }
        });
        return new ResponseResult<>(0, present.get(), null);
    }
    private List<String> tablejg(String env,String dbType) {
        List<String> sqls = new ArrayList<>();
        List<MatchedTableItem> matchedTableItems = compareStructure(env, dbType);
        for (MatchedTableItem matchedTableItem : matchedTableItems) {
            String sql = matchedTableItem.getSql();
            if(!StringUtils.isEmpty(sql)){
                sqls.add(sql);
            }
            List<MatchedColumnItem> columns = matchedTableItem.getColumns();
            if (!CollectionUtils.isEmpty(columns)) {
                for (MatchedColumnItem matchedColumnItem : columns) {
                    String sql1 = matchedColumnItem.getSql();
                    if(!StringUtils.isEmpty(sql1)){
                        sqls.add(sql1);
                    }
                }
            }
            List<MatchedIndexItem> indexs = matchedTableItem.getIndexs();
            if (!CollectionUtils.isEmpty(indexs)) {
                for (MatchedIndexItem matchedIndexItem : indexs) {
                    List<String> sql2s = matchedIndexItem.getSqls();
                    if(!CollectionUtils.isEmpty(sql2s)){
                        sqls.addAll(sql2s);
                    }
                }
            }
        }
        return sqls;
    }
    @RequestMapping("syncDb")
    public ResponseResult<String> syncDb(@RequestParam("userId") String userId,@RequestParam("env") String env) {
        WebSocketManager.sendMessage2(userId,"开始比对data库表结构...");
        //比较data表结构
        List<String> sqls = tablejg(env,"data");
        WebSocketManager.sendMessage2(userId,"比对data库表结构完成");
        if (!CollectionUtils.isEmpty(sqls)) {
            WebSocketManager.sendMessage2(userId,"开始同步data库表结构:"+sqls.size());
            //同步data表结构
            JdbcTemplate rightJdbcTemplate = CompareJdbcTemplate.getRightJdbcTemplate();
            for (String sql : sqls) {
                rightJdbcTemplate.execute(sql);
            }
            WebSocketManager.sendMessage2(userId,"同步data库表结构完成");
        }else{
            WebSocketManager.sendMessage2(userId,"data库表结构一致");
        }
        WebSocketManager.sendMessage2(userId,"开始比对data库数据");
        //比较data数据
        List<CompareData> compareDatas = compareT(userId, env, "data",true);
        List<String> excludeTables = new ArrayList<>();
        excludeTables.add("t_generator");
        excludeTables.add("t_server_node");
        compareDatas = compareDatas.stream().filter(t->!excludeTables.contains(t.getTableName())).collect(Collectors.toList());
        JdbcTemplate secondJdbcTemplate = CompareJdbcTemplate.getRightJdbcTemplate();
        WebSocketManager.sendMessage2(userId,"开始比对data库数据完成");
        if (!CollectionUtils.isEmpty(compareDatas)) {
            WebSocketManager.sendMessage2(userId,"开始同步data库数据");
            //同步data数据
            for (CompareData compareData : compareDatas) {
                List<String> execSqls = compareData.getExecSqls();
                WebSocketManager.sendMessage2(userId,"同步表数据===============>>"+compareData.getTableName());
                if (!CollectionUtils.isEmpty(execSqls)) {
                    for (String sql : execSqls) {
                        secondJdbcTemplate.execute(sql);
                    }
                }
            }
            WebSocketManager.sendMessage2(userId,"同步data库数据完成");
        }else{
            WebSocketManager.sendMessage2(userId,"data库数据一致");
        }

        //比较game表结构
        List<String> sqls2 = tablejg(env,"game");
        WebSocketManager.sendMessage2(userId, "比对game库表结构完成");
        if (!CollectionUtils.isEmpty(sqls2)) {
            WebSocketManager.sendMessage2(userId,"开始同步game库表结构:"+sqls2.size());
            //同步data表结构
            JdbcTemplate rightJdbcTemplate = CompareJdbcTemplate.getRightJdbcTemplate();
            for (String sql : sqls2) {
                rightJdbcTemplate.execute(sql);
            }
            WebSocketManager.sendMessage2(userId,"同步game库表结构完成");
        }else{
            WebSocketManager.sendMessage2(userId,"game库表结构一致");
        }
        //同步game表结构
        return new ResponseResult<>(0, "sync success", null);
    }

    @RequestMapping("compare2")
    public ResponseResult<List<CompareData>> compare2(@RequestParam("userId") String userId,@RequestParam("env") String env,@RequestParam("dbType") String dbType){
        List<CompareData> compareData = compareT(userId, env, dbType,false);
        return new ResponseResult<>(0, compareData, null);
    }
    private List<CompareData>  compareT(String userId,String env, String dbType,boolean sync){
        JdbcTemplate leftJdbcTemplate;
        String leftDataBase;
        String rightDataBase;
        if("test".equals(env) && "data".equals(dbType)){
            leftJdbcTemplate = JdbcUtil.getDefaultDataJdbc("test");
            leftDataBase = "d11_data_test";
            rightDataBase = "d11_data_gs1";
        }else{
            leftJdbcTemplate = JdbcUtil.getDefaultDataJdbc("dev");
            leftDataBase = "d11_data_dev";
            rightDataBase = "d11_data_test";
        }
        CompareJdbcTemplate.setLeftDataBaseName(leftDataBase);
        CompareJdbcTemplate.setRightDataBaseName(rightDataBase);
        List<CompareData> compareData = databaseStructureLoader.loadColumns(leftJdbcTemplate, leftDataBase,userId,sync);
        SqlManager.setCompareData(compareData);
        return compareData;
    }

    /**
     * 表结构对比
     */
    @RequestMapping("compare")
    public ResponseResult<List<MatchedTableItem>> compare(@RequestParam("env") String env,@RequestParam("dbType") String dbType){
        List<MatchedTableItem> matchedTableItems = compareStructure(env, dbType);
        return new ResponseResult<>(0,matchedTableItems,null);
    }
    private List<MatchedTableItem> compareStructure(String env, String dbType) {
        long start = System.currentTimeMillis();
        DatabaseComparator comparator = new DatabaseComparator();
        JdbcTemplate leftJdbcTemplate;
        JdbcTemplate rightJdbcTemplate;
        String leftDataBase;
        String rightDataBase;
        if("dev".equals(env) && "game".equals(dbType)){
            leftJdbcTemplate = JdbcUtil.getDefaultGameJdbc("dev");
            rightJdbcTemplate = JdbcUtil.getDefaultGameJdbc("test");
            leftDataBase = "d11_game_dev";
            rightDataBase = "d11_game_test";
        }else if("test".equals(env) && "data".equals(dbType)){
            leftJdbcTemplate = JdbcUtil.getDefaultDataJdbc("test");
            rightJdbcTemplate = JdbcUtil.getDefaultDataJdbc("gs1");
            leftDataBase = "d11_data_test";
            rightDataBase = "d11_data_gs1";
        }else if("test".equals(env) && "game".equals(dbType)){
            leftJdbcTemplate = JdbcUtil.getDefaultGameJdbc("test");
            rightJdbcTemplate = JdbcUtil.getDefaultGameJdbc("gs1");
            leftDataBase = "d11_game_test";
            rightDataBase = "d11_game_gs1";
        }else{
            leftJdbcTemplate = JdbcUtil.getDefaultDataJdbc("dev");
            rightJdbcTemplate = JdbcUtil.getDefaultDataJdbc("test");
            leftDataBase = "d11_data_dev";
            rightDataBase = "d11_data_test";
        }
        CompareJdbcTemplate.setLeftJdbcTemplate(leftJdbcTemplate);
        CompareJdbcTemplate.setRightJdbcTemplate(rightJdbcTemplate);
        CompareJdbcTemplate.setLeftDataBaseName(leftDataBase);
        CompareJdbcTemplate.setRightDataBaseName(rightDataBase);
        Database left_db = databaseStructureLoader.loadDatabase(leftJdbcTemplate,leftDataBase);
        Database rigth_db = databaseStructureLoader.loadDatabase(rightJdbcTemplate,rightDataBase);

        log.info(String.format("加载表结构数据成功.... 耗时:%s ms",System.currentTimeMillis() - start));

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
        SqlManager.setMatchedTableItems(matchedTableItems);
        return  matchedTableItems;
    }
}
