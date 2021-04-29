package com.tool.util;

/** * 分表工具 */
public class TableUtil {

    /**
     * * 根据球队id获取分表表名
     *
     * @param tableName 表名
     * @param tablePartNum 分表数量
     * @param teamId 球队id
     * @return
     */
    public static String getTableByTeamId(String tableName, int tablePartNum, String teamId) {
        return tableName + "_" + getTablePart(teamId, tablePartNum);
    }

    public static int getTablePart(String teamId, int tablePartNum) {
        return Math.abs(teamId.hashCode()) % tablePartNum;
    }
}
