package com.tool.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "tool")
public class ToolConfig {

    private int gameType;
    private String channel;
    private String channelInfo;
    private String chooseServerUrl;
    private String loginValidUrl;
    private String defaultPassword;
    private String registerUrl;
    private String updateDatePrefix;
    private String gsPath;
    private String httpPrefix;
    private String httpSuffix;

    public String getHttpPrefix() {
        return httpPrefix;
    }

    public void setHttpPrefix(String httpPrefix) {
        this.httpPrefix = httpPrefix;
    }

    public String getHttpSuffix() {
        return httpSuffix;
    }

    public void setHttpSuffix(String httpSuffix) {
        this.httpSuffix = httpSuffix;
    }

    public String getGsPath() {
        return gsPath;
    }

    public void setGsPath(String gsPath) {
        this.gsPath = gsPath;
    }

    public int getGameType() {
        return gameType;
    }

    public void setGameType(int gameType) {
        this.gameType = gameType;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getChannelInfo() {
        return channelInfo;
    }

    public void setChannelInfo(String channelInfo) {
        this.channelInfo = channelInfo;
    }

    public String getChooseServerUrl() {
        return chooseServerUrl;
    }

    public void setChooseServerUrl(String chooseServerUrl) {
        this.chooseServerUrl = chooseServerUrl;
    }

    public String getLoginValidUrl() {
        return loginValidUrl;
    }

    public void setLoginValidUrl(String loginValidUrl) {
        this.loginValidUrl = loginValidUrl;
    }

    public String getDefaultPassword() {
        return defaultPassword;
    }

    public void setDefaultPassword(String defaultPassword) {
        this.defaultPassword = defaultPassword;
    }

    public String getRegisterUrl() {
        return registerUrl;
    }

    public void setRegisterUrl(String registerUrl) {
        this.registerUrl = registerUrl;
    }

    public String getUpdateDatePrefix() {
        return updateDatePrefix;
    }

    public void setUpdateDatePrefix(String updateDatePrefix) {
        this.updateDatePrefix = updateDatePrefix;
    }
}
