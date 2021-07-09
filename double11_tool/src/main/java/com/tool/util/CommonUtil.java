package com.tool.util;

import org.apache.poi.ss.formula.functions.T;
import org.nfunk.jep.JEP;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtil {
    private static Pattern linePattern = Pattern.compile("_(\\w)");

    /** 下划线转驼峰 */
    public static String lineToHump(String str) {
        str = str.toLowerCase();
        Matcher matcher = linePattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        String s = sb.toString();
        s= s.substring(1).replace("Rule", "");
        return s;
    }


    private static Pattern humpPattern = Pattern.compile("[A-Z]");

    /** 驼峰转下划线,效率比上面高 */
    public static String humpToLine(String str) {
        List<String> specialTables = new ArrayList<String>(){{
            add("Item");
            add("Player");
            add("LeagueNpcTeam");
        }};
        Matcher matcher = humpPattern.matcher(str);
        StringBuffer sb = new StringBuffer("t");
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        if(!specialTables.contains(str)){
            sb.append("_rule");
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println(lineToHump("t_mission_trigger_rule"));// f_parent_no_leader
        System.out.println(humpToLine("MissionTrigger"));// f_parent_no_leader
        //
        }
}
