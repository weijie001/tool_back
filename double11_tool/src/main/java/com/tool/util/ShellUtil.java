package com.tool.util;

public class ShellUtil {

    public static String[] getCommand(String command) {
        String os = System.getProperty("os.name");
        String shell = "/bin/bash";
        String c = "-c";
        String[] cmd = { shell, c, command };
        return cmd;
    }
}
