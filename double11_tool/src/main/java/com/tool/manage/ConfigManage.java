package com.tool.manage;

import com.tool.bean.Config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigManage {
    private static Map<String, Config> configMaps = new HashMap<>();

    public static void initConfig(List<Config> configs) {
        configMaps = configs.stream().collect(Collectors.toMap(Config::getConfigKey,config -> config));
    }

    public static Config getConfig(String key) {
        return configMaps.get(key);
    }
}




