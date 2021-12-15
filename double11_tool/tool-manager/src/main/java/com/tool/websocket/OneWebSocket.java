package com.tool.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 前后端交互的类实现消息的接收推送(自己发送给自己)
 * 
 * @ServerEndpoint(value = "/test/one") 前端通过此URI和后端交互，建立连接
 */
@ServerEndpoint(value = "/websocket/{userId}")
@Component
public class OneWebSocket {
    private static final Logger log = LoggerFactory.getLogger(OneWebSocket.class);

    /**
     * 记录当前在线连接数
     */
    private static AtomicInteger onlineCount = new AtomicInteger(0);

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(@PathParam("userId")String userId,Session session) {
        onlineCount.incrementAndGet(); // 在线数加1
        WebSocketManager.addSession(userId,session);
        log.info("有新连接加入：{}，当前在线人数为：{},userId:{}", session.getId(), onlineCount.get(),userId);
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam("userId")String userId,Session session) {
        onlineCount.decrementAndGet(); // 在线数减1
        WebSocketManager.removeSession(userId);
        log.info("有一连接关闭：{}，当前在线人数为：{},userId:{}", session.getId(), onlineCount.get(),userId);
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("服务端收到客户端[{}]的消息:{}", session.getId(), message);
        this.sendMessage("Hello, " + message, session);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误");
        error.printStackTrace();
    }

    /**
     * 服务端发送消息给客户端
     */
    private void sendMessage(String message, Session toSession) {
        try {
            log.info("服务端给客户端[{}]发送消息{}", toSession.getId(), message);
            toSession.getBasicRemote().sendText(message);
        } catch (Exception e) {
            log.error("服务端发送消息给客户端失败：{}", e);
        }
    }
}