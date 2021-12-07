package com.tool.compare.context;

import com.tool.compare.model.Column;
import org.apache.commons.lang3.StringUtils;

public class MatchedColumnItem extends AbstractMatchedItem {
    private String sql;
    private Column left;

    private Column right;

    private CompareResult result;

    /**
     * @return the result
     */
    public CompareResult getResult() {
        return result;
    }

    /**
     * @return the left
     */
    public Column getLeft() {
        return left;
    }

    /**
     * @param left
     *            the left to set
     */
    public void setLeft(Column left) {
        this.left = left;
    }

    /**
     * @return the right
     */
    public Column getRight() {
        return right;
    }

    /**
     * @param right
     *            the right to set
     */
    public void setRight(Column right) {
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
            this.sql = "alter table " + right.getTableName() + " drop column " + right.getName()+";";
            return result;
        }
        if (right == null) {
            result = CompareResult.RIGHT_NOT_EXIST;
            this.sql = getSql(left,"add");
            return result;
        }
        result = CompareResult.NOT_EQUAL;
        if (StringUtils.equals(left.getType(), right.getType())
                && StringUtils.equals(left.getComment(), right.getComment())
                && StringUtils.equals(left.getDefaultValue(), right.getDefaultValue())
                && StringUtils.equals(left.getLength(), right.getLength())
                && left.isNotNull() == right.isNotNull()
                && left.getScale() == right.getScale()
                && left.getOrdinalPosition() == right.getOrdinalPosition()) {
            result = CompareResult.EQUAL;
        } else {
            this.sql = getSql(left,"modify");
        }
        return result;
    }

    private String getSql(Column column,String operate) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("alter table ")
                .append(column.getTableName())
                .append(" ")
                .append(operate)
                .append(" column ")
                .append(column.getName())
                .append(" ")
                .append(column.getType())
                .append("(")
                .append(column.getLength())
                .append(") ");

        boolean notNull = column.isNotNull();
        if (notNull) {
            stringBuilder.append("not ");
        }
        stringBuilder.append("null ");
        String defaultValue = column.getDefaultValue();
        if (!StringUtils.isEmpty(defaultValue)) {
            stringBuilder.append("default ");
            if (column.getType().endsWith("int")) {
                stringBuilder.append(defaultValue).append(" ");
            } else {
                stringBuilder.append("'").append(defaultValue).append("' ");
            }
        }
        String comment = column.getComment();
        if (!StringUtils.isEmpty(comment)) {
            stringBuilder.append("comment '").append(comment).append("' ");
        }
        stringBuilder.append("after ").append(column.getPrefixName());
        stringBuilder.append(";");
        return stringBuilder.toString();
    }
}
