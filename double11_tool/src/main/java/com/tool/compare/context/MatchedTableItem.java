package com.tool.compare.context;


import com.tool.compare.model.Table;
import com.tool.util.JdbcUtil;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

public class MatchedTableItem extends AbstractMatchedItem {
    int id;

    /**
     * 左边表
     */
    Table left;

    /**
     * 右边表
     */
    Table right;

    List<MatchedColumnItem> columns;

    /***
     * 索引
     */
    List<MatchedIndexItem> indexs;

    private String sql;

    private CompareResult result;

    /**
     * @return the result
     */
    public CompareResult getResult() {
        return result;
    }

    /**
     * @return the columns
     */
    public List<MatchedColumnItem> getColumns() {
        return columns;
    }

    /**
     * @param columns
     *            the columns to set
     */
    public void setColumns(List<MatchedColumnItem> columns) {
        this.columns = columns;
    }

    /**
     * @return the indexs
     */
    public List<MatchedIndexItem> getIndexs() {
        return indexs;
    }

    /**
     * @param indexs
     *            the indexs to set
     */
    public void setIndexs(List<MatchedIndexItem> indexs) {
        this.indexs = indexs;
    }

    /**
     * @return the left
     */
    public Table getLeft() {
        return left;
    }

    /**
     * @param left
     *            the left to set
     */
    public void setLeft(Table left) {
        this.left = left;
    }

    /**
     * @return the right
     */
    public Table getRight() {
        return right;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    /**
     * @param right
     *            the right to set
     */
    public void setRight(Table right) {
        this.right = right;
    }
    @Override
    public CompareResult compare() {
        if (left == null) {
            result = CompareResult.LEFT_NOT_EXIST;
            this.sql = "drop table " + right.getName();
            return result;
        }
        if (right == null) {
            result = CompareResult.RIGHT_NOT_EXIST;
            String exeSql = "show create table " + left.getName();
            JdbcTemplate leftJdbcTemplate = JdbcUtil.getDefaultDataJdbc("dev");
            List<Map<String, Object>> maps = leftJdbcTemplate.queryForList(exeSql);
            this.sql = (String)maps.get(0).get("Create Table");
            return result;
        }
        result = CompareResult.EQUAL;

        for (MatchedColumnItem columnItem: columns){
            if(CompareResult.EQUAL != columnItem.compare()){
                result = CompareResult.NOT_EQUAL;
            }
        }

        for (MatchedIndexItem indexItem : indexs) {
            if (CompareResult.EQUAL != indexItem.compare()) {
                result = CompareResult.NOT_EQUAL;
            }
        }
        return result;
    }
}
