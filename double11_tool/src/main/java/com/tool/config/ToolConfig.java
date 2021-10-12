package com.tool.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "tool")
public class ToolConfig {

    private int gameType;
    private String channel;
    private String channelInfo;
    private String chooseServer;
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

    public String getChooseServer() {
        return chooseServer;
    }

    public void setChooseServer(String chooseServer) {
        this.chooseServer = chooseServer;
    }
}
