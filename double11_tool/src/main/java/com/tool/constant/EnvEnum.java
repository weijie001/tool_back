package com.tool.constant;

public enum EnvEnum {
    DEV("dev","d11_data_dev","d11_game_dev"),
    TEST("test","d11_data_test","d11_data_test");
    private String env;
    private String data;
    private String game;
    public String getEnv() {
        return env;
    }

    public String getData() {
        return data;
    }

    public String getGame() {
        return game;
    }

    EnvEnum(String env, String data, String game) {
        this.env = env;
        this.data = data;
        this.game = game;
    }
}
