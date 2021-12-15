package com.tool.compare;


import com.tool.compare.context.*;
import com.tool.compare.model.Column;
import com.tool.compare.model.Database;
import com.tool.compare.model.Index;
import com.tool.compare.model.Table;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class DatabaseComparator {
    public MatchedDatabaseContext compare(Database left, Database right) {
        MatchedDatabaseContext context = matcheDatabase(left, right);
        List<MatchedTableItem> tableItems = context.getTables();
        for (MatchedTableItem tableItem : tableItems) {
            tableItem.compare();
        }
        return context;
    }

    /**
     * 进行匹配 库索引和表
     *
     * @param left
     * @param right
     * @return
     */
    private MatchedDatabaseContext matcheDatabase(Database left, Database right) {

        MatchedDatabaseContext context = new MatchedDatabaseContext();

        MatchedDatabaseItem databaseItem = new MatchedDatabaseItem();
        databaseItem.setLeft(left);
        databaseItem.setRight(right);
        context.setDatabase(databaseItem);
        List<MatchedTableItem> tableItems = new ArrayList<>();
        int index = 1;
        // 匹配表
        Set<Table> rightMatchedTables = new HashSet<>();
        for (Table leftTable : left.getTables()) {
            MatchedTableItem tableItem = null;
            for (Table rightTable : right.getTables()) {
                if (StringUtils.equalsIgnoreCase(leftTable.getName(), rightTable.getName())) {
                    tableItem = new MatchedTableItem();
                    tableItem.setId(index++);
                    tableItem.setLeft(leftTable);
                    tableItem.setRight(rightTable);
                    rightMatchedTables.add(rightTable);
                    break;
                }
            }
            if (tableItem == null) {// 未匹配上
                tableItem = new MatchedTableItem();
                tableItem.setId(index++);
                tableItem.setLeft(leftTable);
            }

            tableItems.add(tableItem);
            matcheTable(tableItem);// 匹配表格
        }
        for (Table rightTable : right.getTables()) {
            if (!rightMatchedTables.contains(rightTable)) {
                MatchedTableItem tableItem = new MatchedTableItem();
                tableItem.setId(index++);
                tableItem.setRight(rightTable);
                tableItems.add(tableItem);
            }
        }
        context.setTables(tableItems);
        return context;
    }


    /***
     * 匹配表格内的列和索引
     *
     * @param tableItem
     */
    private void matcheTable(MatchedTableItem tableItem) {
        if (tableItem.getLeft() == null || tableItem.getRight() == null) {
            return;
        }

        // 匹配列
        List<MatchedColumnItem> columnItems = matcheColumns(tableItem.getLeft().getColumns(),
                tableItem.getRight().getColumns(), false);
        tableItem.setColumns(columnItems);

        // 匹配索引
        List<MatchedIndexItem> indexItems = matcheIndexs(tableItem.getLeft().getIndexs(),
                tableItem.getRight().getIndexs());
        tableItem.setIndexs(indexItems);
    }

    private List<MatchedIndexItem> matcheIndexs(List<Index> leftIndexs, List<Index> rigthIndexs) {
        List<MatchedIndexItem> indexItems = new ArrayList<>();
        if (leftIndexs == null) {
            leftIndexs = new ArrayList<>();
        }
        if (rigthIndexs == null) {
            rigthIndexs = new ArrayList<>();
        }

        List<Index> matchedRigthIndex = new ArrayList<>();
        for (Index leftIndex : leftIndexs) {
            MatchedIndexItem indexItem = null;
            for (Index rigthIndex : rigthIndexs) {
                if (StringUtils.equals(leftIndex.getName(), rigthIndex.getName())) {
                    indexItem = new MatchedIndexItem();
                    indexItem.setLeft(leftIndex);
                    indexItem.setRight(rigthIndex);
                    matchedRigthIndex.add(rigthIndex);
                }
            }
            if (indexItem == null) {
                indexItem = new MatchedIndexItem();
                indexItem.setLeft(leftIndex);
            }
            indexItems.add(indexItem);
        }
        for (Index rigthIndex : rigthIndexs) {
            if (!matchedRigthIndex.contains(rigthIndex)) {
                MatchedIndexItem indexItem = new MatchedIndexItem();
                indexItem.setRight(rigthIndex);
                indexItems.add(indexItem);
            }
        }

        return indexItems;
    }
    
    private List<MatchedColumnItem> matcheColumns(List<Column> leftColumns, List<Column> rightColumns,
                                                  boolean sequence) {
        if (!sequence) {
            return matcheColumnsIgnoreSequence(leftColumns, rightColumns);
        }
        return matcheColumnsRequireSequence(leftColumns, rightColumns);
    }


    /***
     * 匹配列 关心列顺序
     *
     * @param leftColumns
     * @param rightColumns
     * @return
     */
    private List<MatchedColumnItem> matcheColumnsRequireSequence(List<Column> leftColumns, List<Column> rightColumns) {
        throw new RuntimeException("此方法未实现");
    }


    /***
     * 匹配列忽略顺序
     *
     * @param leftColumns
     * @param rightColumns
     * @return
     */
    private List<MatchedColumnItem> matcheColumnsIgnoreSequence(List<Column> leftColumns, List<Column> rightColumns) {
        List<MatchedColumnItem> indexItems = new ArrayList<>();
        if (leftColumns == null) {
            leftColumns = new ArrayList<>();
        }
        if (rightColumns == null) {
            rightColumns = new ArrayList<>();
        }
        List<Column> matchedRigthColumn = new ArrayList<>();
        for (Column leftColumn : leftColumns) {
            MatchedColumnItem columnItem = null;
            for (Column rigthColum : rightColumns) {
                if (StringUtils.equals(leftColumn.getName(), rigthColum.getName())) {
                    columnItem = new MatchedColumnItem();
                    columnItem.setLeft(leftColumn);
                    columnItem.setRight(rigthColum);
                    matchedRigthColumn.add(rigthColum);
                }
            }
            if (columnItem == null) {
                columnItem = new MatchedColumnItem();
                columnItem.setLeft(leftColumn);
            }
            indexItems.add(columnItem);
        }
        for (Column rigthColumn : rightColumns) {
            if (!matchedRigthColumn.contains(rigthColumn)) {
                MatchedColumnItem indexItem = new MatchedColumnItem();
                indexItem.setRight(rigthColumn);
                indexItems.add(indexItem);
            }
        }
        return indexItems;
    }
}
