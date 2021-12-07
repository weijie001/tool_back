package com.tool.compare.context;

import java.util.List;

public class SqlManager {

    public static List<CompareData> compareData;
    public static List<MatchedTableItem> matchedTableItems;
    public static List<CompareData> getCompareData() {
        return compareData;
    }

    public static void setCompareData(List<CompareData> compareData) {
        SqlManager.compareData = compareData;
    }

    public static List<MatchedTableItem> getMatchedTableItems() {
        return matchedTableItems;
    }

    public static void setMatchedTableItems(List<MatchedTableItem> matchedTableItems) {
        SqlManager.matchedTableItems = matchedTableItems;
    }
}
