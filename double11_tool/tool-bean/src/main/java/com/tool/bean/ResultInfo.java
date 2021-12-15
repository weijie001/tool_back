package com.tool.bean;

import java.util.List;

public class ResultInfo {
    List<String> sqls;
    List<String> errors;

    List<TableData> data;

    String sqlName;

    public List<String> getSqls() {
        return sqls;
    }

    public void setSqls(List<String> sqls) {
        this.sqls = sqls;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public List<TableData> getData() {
        return data;
    }

    public void setData(List<TableData> data) {
        this.data = data;
    }

    public String getSqlName() {
        return sqlName;
    }

    public void setSqlName(String sqlName) {
        this.sqlName = sqlName;
    }
}
