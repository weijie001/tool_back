package com.tool.bean;

import java.util.List;

public class QueryData {
     private PageList pageList;
    private List<Object> columns;

    public PageList getPageList() {
        return pageList;
    }

    public void setPageList(PageList pageList) {
        this.pageList = pageList;
    }

    public List<Object> getColumns() {
        return columns;
    }

    public void setColumns(List<Object> columns) {
        this.columns = columns;
    }
}
