package com.tool.compare.context;

import com.tool.compare.model.Index;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class MatchedIndexItem extends AbstractMatchedItem {
    private List<String> sqls = new ArrayList<>();
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

    public List<String> getSqls() {
        return sqls;
    }

    public void setSqls(List<String> sqls) {
        this.sqls = sqls;
    }

    @Override
    public CompareResult compare() {
        if (left == null) {
            result = CompareResult.LEFT_NOT_EXIST;
            this.sqls.add("alter table `" + right.getTableName() + "` drop index `"+ right.getName() + "`;");
            //this.sql = "alter table `" + right.getTableName() + "` drop index `"+ right.getName() + "`;";
            return result;
        }
        if (right == null) {
            result = CompareResult.RIGHT_NOT_EXIST;
            String columnsStr = String.join(",", left.getColumns());
            columnsStr = "`" + columnsStr.replace(",", "`,`") + "`";
            this.sqls.add("alter table `" + left.getTableName() + "` add index `" + left.getName() + "`(" + columnsStr + ") using " + left.getIndexMethod() + ";");
            //this.sql = "alter table `" + left.getTableName() + "` add index `" + left.getName() + "`(" + columnsStr + ") using " + left.getIndexMethod() + ";";
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
            columnsStr = "`" + columnsStr.replace(",", "`,`") + "`";
            if ("PRIMARY".equals(name)) {
                this.sqls.add("alter table `" + left.getTableName() + "` drop primary key;");
                //this.sql = "alter table `" + left.getTableName() + "` drop primary key;\n";
                this.sqls.add("alter table `"+ left.getTableName() + "` add primary key ("+columnsStr+");");
                //this.sql+="alter table `"+ left.getTableName() + "` add primary key ("+columnsStr+");";
            }else{
                //修改索引
                this.sqls.add("alter table `"+ left.getTableName() + "` add primary key ("+columnsStr+");");
                //this.sql = "alter table `" + left.getTableName() + "` drop index `" + left.getName() + "`,add index `" + left.getName() + "`(" + columnsStr + ") using " + left.getIndexMethod() + ";";
                System.out.println();
            }
        }
        return result;
    }
}
