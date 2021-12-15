package com.tool.compare;

import com.tool.BaseService;
import com.tool.compare.context.*;
import com.tool.compare.model.Database;
import com.tool.util.JdbcUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CompareService extends BaseService {
    @Autowired
    private DatabaseStructureLoader databaseStructureLoader;

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
