package com.tool.compare.loader;


import com.tool.compare.context.CompareData;
import com.tool.compare.context.CompareJdbcTemplate;
import com.tool.compare.model.Column;
import com.tool.compare.model.Database;
import com.tool.compare.model.Index;
import com.tool.compare.model.Table;
import com.tool.websocket.WebSocketManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;

import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class MysqlStructureLoader implements StructureLoader {

    public Database  loadDatabaseStructure(JdbcTemplate JdbcTemplate,String dataBaseName){
        Database database = new Database(dataBaseName);

        List<Table> tables = loadTables(JdbcTemplate, dataBaseName);

        database.setTables(tables);
        return database;
    }

    public List<String> loadDatabaseNames(Connection connection) throws Exception {
        return null;
    }
    private String getCompareSql(List<String> columns,String leftDataBase,String rightDataBase,String tableName) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("select * from ").append(leftDataBase).append(".").append(tableName).append(" t1 where not EXISTS( select 1 from ").
                append(rightDataBase).append(".").append(tableName).append(" t2 where 1 = 1");
        for (String str : columns) {
            stringBuilder.append(" and ifnull(t1.")
                    .append(str)
                    .append(",'')= ifnull(t2.")
                    .append(str)
                    .append(",'') ");
        }
        stringBuilder.append(");");
        return stringBuilder.toString();
    }
    public String getTimestampValue(String value) {
        SimpleDateFormat originFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date originDate = originFmt.parse(value);
            value = originFmt.format(originDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public List<CompareData> compareData(JdbcTemplate jdbcTemplate, String databaseName,String userId,boolean sync) {
        List<Table> tables = getAllTables(jdbcTemplate, databaseName);
        List<CompareData> compareDatas = new ArrayList<>();
        long l = System.currentTimeMillis();
        System.out.println("开始对比数据");
        int index = 1;
        int size = tables.size();
        if (sync) {
            WebSocketManager.sendMessage2(userId, "==========>>          ");
        }
        for (Table table : tables) {
            int percent = index*100/size;
            if (sync) {
                WebSocketManager.sendMessage2(userId, "compare table"+table.getName()+"$"+index);
            }else{
                WebSocketManager.sendMessage2(userId, percent +"$"+table.getName());
            }
            try {
                loadColumns(jdbcTemplate, databaseName, table.getName(), compareDatas);
            } catch (Exception e) {
                e.printStackTrace();
            }
            index++;
        }
        if (!sync){
            WebSocketManager.sendMessage2(userId,"100$ok");
        }else{
            WebSocketManager.sendMessage2(userId," ");
        }
        System.out.println("数据对比时间"+(System.currentTimeMillis()-l));
        return compareDatas;
    }
    public void loadColumns(JdbcTemplate jdbcTemplate, String databaseName,String tableName,List<CompareData> compareDatas) {
        String colSql = "SELECT COLUMN_NAME AS `name` from information_schema.`COLUMNS` where TABLE_SCHEMA  =  '" + databaseName + "' and table_name ='"+tableName+"'";
        List<String> columns = jdbcTemplate.query(colSql, (resultSet, i) -> resultSet.getString("name"));
        String compareSql2 = getCompareSql(columns, CompareJdbcTemplate.getRightDataBaseName(), CompareJdbcTemplate.getLeftDataBaseName(),tableName);
        List<Map<String, Object>> removeLists = jdbcTemplate.queryForList(compareSql2);
        List<String> deleteSqls = new ArrayList<>();
        for (Map<String, Object> map : removeLists) {
            StringBuilder stringBuilder1 = new StringBuilder();
            stringBuilder1.append("delete from `").append(tableName).append("` where 1 = 1 ");
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Object value = entry.getValue();
                if(value instanceof Timestamp) {
                    value = getTimestampValue(String.valueOf(value));
                }
                if (value == null) {
                    stringBuilder1.append(" and `").append(entry.getKey()).append("` is null");
                }else{
                    stringBuilder1.append(" and `").append(entry.getKey()).append("` =  '").append(entry.getValue()).append("'");
                }
            }
            stringBuilder1.append(";");
            String replace = stringBuilder1.toString().replace("1 = 1 and", "");
            deleteSqls.add(replace);
        }
        String compareSql = getCompareSql(columns, CompareJdbcTemplate.getLeftDataBaseName(), CompareJdbcTemplate.getRightDataBaseName(),tableName);
        List<Map<String, Object>> maps = jdbcTemplate.queryForList(compareSql);
        List<String> insertSqls = new ArrayList<>();
        for (Map<String, Object> map : maps) {
            StringBuilder stringBuilder1 = new StringBuilder();
            stringBuilder1.append("insert into `").append(tableName).append("`(");
            StringBuilder stringBuilder2 = new StringBuilder();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                stringBuilder1.append("`").append(key).append("`").append(",");
                Object value = entry.getValue();

                if(value instanceof Timestamp) {
                    value = getTimestampValue(String.valueOf(value));
                }
                if (value == null) {
                    stringBuilder2.append("NULL").append(",");
                }else{
                    String str = String.valueOf(value);
                    if(str.contains("'")){
                        str = str.replace("'","\\\'");
                    }
                    stringBuilder2.append("'").append(str).append("'").append(",");
                }
            }
            String substring = stringBuilder2.substring(0, stringBuilder2.length() - 1);
            String insertSql = new StringBuilder(stringBuilder1.substring(0, stringBuilder1.length() - 1)).append(") values(").append(substring).append(");").toString();
            insertSqls.add(insertSql);
        }
        deleteSqls.addAll(insertSqls);
        if (!CollectionUtils.isEmpty(deleteSqls)) {
            CompareData compareData = new CompareData();
            compareData.setTableName(tableName);
            compareData.setExecSqls(deleteSqls);
            compareDatas.add(compareData);
        }
    }
    private List<Table> getAllTables(JdbcTemplate jdbcTemplate, String databaseName) {
        String tableSql = "SELECT TABLE_NAME as `name` , `ENGINE` as `engine`,TABLE_COLLATION  as `charset`  from information_schema.`TABLES` where TABLE_SCHEMA  = '"+databaseName+"'  and TABLE_TYPE = 'BASE TABLE'";
        List<Table> tables;
        // 加载表结构
        tables = jdbcTemplate.query(tableSql, (resultSet1, i) -> {
            Table table = new Table();
            table.setName(resultSet1.getString("name"));
            table.setCharset(resultSet1.getString("charset"));
            table.setEngine(resultSet1.getString("engine"));
            table.setColumns(new ArrayList<>());
            table.setIndexs(new ArrayList<>());
            return table;
        });
        return tables;
    }
    private List<Table> loadTables(JdbcTemplate jdbcTemplate, String databaseName) {
        // 加载表结构
        List<Table> tables = getAllTables(jdbcTemplate, databaseName);
        Map<String, Table> tableMappper = tables.stream().collect(Collectors.toMap(Table::getName, t -> t));

        String colSql = "SELECT TABLE_NAME as `table_name`,COLUMN_NAME  as `name`,DATA_TYPE as `type` ,IFNULL(CHARACTER_MAXIMUM_LENGTH,NUMERIC_PRECISION) as length ,NUMERIC_SCALE as scale ,COLUMN_COMMENT as `comment` ,COLUMN_DEFAULT as `default_value` ,IS_NULLABLE as `is_nullable`,EXTRA as 'extra',ORDINAL_POSITION as `ordinal_position` from information_schema.`COLUMNS` where TABLE_SCHEMA  =  '"+databaseName+"'";
        List<Column> columns = jdbcTemplate.query(colSql, (resultSet, i) -> {
            Column column = new Column();
            column.setName(resultSet.getString("name"));
            column.setAutoIncrement("auto_increment".equals(resultSet.getString("extra")));
            column.setComment(resultSet.getString("comment"));
            column.setDefaultValue(resultSet.getString("default_value"));
            column.setNotNull("NO".equals(resultSet.getString("is_nullable")));
            column.setType(resultSet.getString("type"));
            column.setLength(resultSet.getString("length")); //数值或者字符串长度
            column.setScale(resultSet.getInt("scale"));    //数值小数点精度
            column.setTableName(resultSet.getString("table_name"));
            column.setOrdinalPosition(resultSet.getInt("ordinal_position"));
            return column;
        });
        Map<String, List<Column>> columnMap = columns.stream().collect(Collectors.groupingBy(Column::getTableName, Collectors.toList()));
        for (Table table : tables) {
            List<Column> columns1 = columnMap.get(table.getName()).stream().sorted(Comparator.comparing(Column::getOrdinalPosition)).collect(Collectors.toList());
            Map<Integer, String> columnsMap = columns1.stream().collect(Collectors.toMap(Column::getOrdinalPosition, t -> t.getName()));
            for (Column column : columns1) {
                String prefixName = columnsMap.get(column.getOrdinalPosition() - 1);
                column.setPrefixName(prefixName);
            }
            if (!CollectionUtils.isEmpty(columns1)) {
                table.setColumns(columns1);
            }
        }
        // 获取索引
        String indexSQL = "SELECT  TABLE_SCHEMA, TABLE_NAME, NON_UNIQUE,INDEX_NAME,SEQ_IN_INDEX,COLUMN_NAME,INDEX_TYPE,CONCAT(COMMENT,INDEX_COMMENT)  INDEX_COMMENT   FROM  INFORMATION_SCHEMA.STATISTICS  WHERE  TABLE_SCHEMA = '"+databaseName+"'   ORDER BY TABLE_SCHEMA,TABLE_NAME,INDEX_NAME,SEQ_IN_INDEX ";
        List<Index> indexss = jdbcTemplate.query(indexSQL, (rs, i) -> {
            Index index = new Index();
            index.setName(rs.getString("INDEX_NAME"));
            index.setColumns(new ArrayList<>());
            index.setIndexMethod(rs.getString("INDEX_TYPE"));
            index.setIndexType(rs.getString("NON_UNIQUE"));
            index.setColumn(rs.getString("COLUMN_NAME"));
            index.setTableName(rs.getString("TABLE_NAME"));
            return index;
        });
        Map<String, List<Index>> indexMap = indexss.stream().collect(Collectors.groupingBy(Index::getTableName, Collectors.toList()));
        //Map<String, Index> indexs = new TreeMap<>();
        for (Map.Entry<String, List<Index>> entry : indexMap.entrySet()) {
            List<Index> value = entry.getValue();
            Map<String, List<Index>> valueMap = value.stream().collect(Collectors.groupingBy(Index::getName, Collectors.toList()));
            for (Map.Entry<String, List<Index>> entry1 : valueMap.entrySet()) {
                Index index1 = entry1.getValue().get(0);
                Index index = new Index();
                index.setName(index1.getName());
                index.setColumns(value.stream().filter(t -> t.getName().equals(index1.getName())).map(Index::getColumn).collect(Collectors.toList()));
                index.setIndexMethod(index1.getIndexMethod());
                index.setIndexType(index1.getIndexType());
                index.setTableName(index1.getTableName());
                Table table = tableMappper.get(index.getTableName());
                if (table != null) {
                    table.getIndexs().add(index);
                }
            }
        }
        return tables;
    }
}
