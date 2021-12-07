package com.tool.compare.context;

import com.tool.compare.model.Index;
import org.apache.commons.lang3.StringUtils;

public class MatchedIndexItem extends AbstractMatchedItem {
    private String sql;
    /**
     * 左边
     */
    Index left;

    /**
     * 右边
     */
    Index right;


    private CompareResult result;

    /**
     * @return the left
     */
    public Index getLeft() {
        return left;
    }

    /**
     * @param left
     *            the left to set
     */
    public void setLeft(Index left) {
        this.left = left;
    }

    /**
     * @return the right
     */
    public Index getRight() {
        return right;
    }

    /**
     * @param right
     *            the right to set
     */
    public void setRight(Index right) {
        this.right = right;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    @Override
    public CompareResult compare() {
        if (left == null) {
            result = CompareResult.LEFT_NOT_EXIST;
            this.sql = "alter table " + right.getTableName() + " drop index "+ right.getName() + ";";
            return result;
        }
        if (right == null) {
            result = CompareResult.RIGHT_NOT_EXIST;
            String columnsStr = String.join(",", left.getColumns());
            this.sql = "alter table " + left.getTableName() + " add index " + left.getName() + "(" + columnsStr + ") using " + left.getIndexMethod() + ";";
            return result;
        }
        result = CompareResult.NOT_EQUAL;

        if (StringUtils.equals(left.getIndexType(), right.getIndexType())
                && StringUtils.equals(left.getIndexMethod(), left.getIndexMethod())
                && left.getColumns().equals(right.getColumns())) {
            result = CompareResult.EQUAL;
        }else {
            System.out.println();
            String name = left.getName();
            String columnsStr = String.join(",", left.getColumns());
            if ("PRIMARY".equals(name)) {
                this.sql = "alter table " + left.getTableName() + " drop primary key;\n";
                this.sql+="alter table "+ left.getTableName() + " add primary key ("+columnsStr+");";
            }else{
                //修改索引
                this.sql = "alter table " + left.getTableName() + " drop index " + left.getName() + ",add index " + left.getName() + "(" + columnsStr + ") using " + left.getIndexMethod() + ";";
                System.out.println();
            }
        }
        return result;
    }
}
