package com.tool.bean;

import java.util.ArrayList;
import java.util.List;

public class TableData {

    private String name;
    private List<Object> columns;
    private List<TableColumn> data = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Object> getColumns() {
        return columns;
    }

    public void setColumns(List<Object> columns) {
        this.columns = columns;
    }

    public List<TableColumn> getData() {
        return data;
    }

    public void setData(List<TableColumn> data) {
        this.data = data;
    }
}
