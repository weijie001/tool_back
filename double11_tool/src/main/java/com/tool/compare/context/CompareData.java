package com.tool.compare.context;

import lombok.Data;

import java.util.List;

@Data
public class CompareData {

    private String tableName;
    private List<String> execSqls;
}
