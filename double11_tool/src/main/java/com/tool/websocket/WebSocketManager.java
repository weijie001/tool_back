package com.tool.websocket;

import javax.websocket.Session;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketManager {

    private static Map<String,Session> sessionMap = new ConcurrentHashMap<>();

    public static Session getSession(String userId) {
        return sessionMap.get(userId);
    }

    public static void addSession(String userId,Session session) {
        sessionMap.put(userId, session);
    }

    public static Map<String, Session> getSessionMap() {
        return sessionMap;
    }

    public static void removeSession(String userId) {
        sessionMap.remove(userId);
    }

    public static void sendMessage(String userId,String message) throws IOException {
        getSession(userId).getBasicRemote().sendText(message);
    }
}
