package com.tool.websocket;

import javax.websocket.Session;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketManager {

    private static Map<String, Session> sessionMap = new ConcurrentHashMap<>();
    private static Map<String, Session> sessionMap2 = new ConcurrentHashMap<>();

    public static Session getSession(String userId) {
        return sessionMap.get(userId);
    }

    public static Session getSession2(String userId) {
        return sessionMap2.get(userId);
    }

    public static void addSession(String userId, Session session) {
        sessionMap.put(userId, session);
    }

    public static void addSession2(String userId, Session session) {
        sessionMap2.put(userId, session);
    }

    public static void removeSession(String userId) {
        sessionMap.remove(userId);
    }

    public static void removeSession2(String userId) {
        sessionMap2.remove(userId);
    }

    public static void sendMessage(String userId, String message) throws IOException {

        getSession(userId).getBasicRemote().sendText(message);
    }

    public static void sendMessage2(String userId, String message){
        try {
            getSession2(userId).getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
