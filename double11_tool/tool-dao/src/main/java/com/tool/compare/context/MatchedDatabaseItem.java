package com.tool.compare.context;


import com.tool.compare.model.Database;

public class MatchedDatabaseItem extends AbstractMatchedItem {

    /**
     * 左边数据库
     */
    Database left;

    /**
     * 右边数据库
     */
    Database right;



    /**
     * @return the left
     */
    public Database getLeft() {
        return left;
    }

    /**
     * @param left the left to set
     */
    public void setLeft(Database left) {
        this.left = left;
    }

    /**
     * @return the right
     */
    public Database getRight() {
        return right;
    }

    /**
     * @param right the right to set
     */
    public void setRight(Database right) {
        this.right = right;
    }

    @Override
    public CompareResult compare() {
        return null;
    }
}
