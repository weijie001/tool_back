package com.tool.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 封装分页对象 
 **/
public class PageList {
    private int page;   //当前页
    private long totalRows;   //总行数
    private long pages;    //总页数
    private List<Map<String,Object>> list=new ArrayList();

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public long getPages() {
        return pages;
    }

    public void setPages(long pages) {
        this.pages = pages;
    }

    public List<Map<String,Object>> getList() {
        if(list==null){
            list=new ArrayList();
        }
        return list;
    }

    public void setList(List<Map<String,Object>> list) {
        this.list = list;
    }

    public long getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(long totalRows) {
        this.totalRows = totalRows;
    }
}