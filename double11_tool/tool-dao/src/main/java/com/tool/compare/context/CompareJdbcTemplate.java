package com.tool.compare.context;

import org.springframework.jdbc.core.JdbcTemplate;

public class CompareJdbcTemplate {
    private static JdbcTemplate leftJdbcTemplate;
    private static JdbcTemplate rightJdbcTemplate;
    private static String leftDataBaseName;
    private static String rightDataBaseName;
    public static JdbcTemplate getLeftJdbcTemplate() {
        return leftJdbcTemplate;
    }

    public static void setLeftJdbcTemplate(JdbcTemplate leftJdbcTemplate) {
        CompareJdbcTemplate.leftJdbcTemplate = leftJdbcTemplate;
    }

    public static JdbcTemplate getRightJdbcTemplate() {
        return rightJdbcTemplate;
    }

    public static void setRightJdbcTemplate(JdbcTemplate rightJdbcTemplate) {
        CompareJdbcTemplate.rightJdbcTemplate = rightJdbcTemplate;
    }

    public static String getLeftDataBaseName() {
        return leftDataBaseName;
    }

    public static void setLeftDataBaseName(String leftDataBaseName) {
        CompareJdbcTemplate.leftDataBaseName = leftDataBaseName;
    }

    public static String getRightDataBaseName() {
        return rightDataBaseName;
    }

    public static void setRightDataBaseName(String rightDataBaseName) {
        CompareJdbcTemplate.rightDataBaseName = rightDataBaseName;
    }
}
