package com.tool.compare;


import com.tool.compare.context.CompareData;
import com.tool.compare.loader.MysqlStructureLoader;
import com.tool.compare.model.Database;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class DatabaseStructureLoader {

    public Database loadDatabase(JdbcTemplate JdbcTemplate,String dataBaseName) {

            return new MysqlStructureLoader().loadDatabaseStructure(JdbcTemplate,dataBaseName);
    }

    public List<CompareData> loadColumns(JdbcTemplate JdbcTemplate, String dataBaseName,String userId,boolean sync) {
         return new MysqlStructureLoader().compareData(JdbcTemplate,dataBaseName,userId,sync);
    }
}
