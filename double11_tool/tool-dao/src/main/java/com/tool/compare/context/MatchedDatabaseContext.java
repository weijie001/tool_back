package com.tool.compare.context;

import java.util.List;

public class MatchedDatabaseContext {

    private MatchedDatabaseItem database;

    private List<MatchedTableItem> tables;

    /**
     * @return the database
     */
    public MatchedDatabaseItem getDatabase() {
        return database;
    }

    /**
     * @param database the database to set
     */
    public void setDatabase(MatchedDatabaseItem database) {
        this.database = database;
    }

    /**
     * @return the tables
     */
    public List<MatchedTableItem> getTables() {
        return tables;
    }

    /**
     * @param tables the tables to set
     */
    public void setTables(List<MatchedTableItem> tables) {
        this.tables = tables;
    }
}
