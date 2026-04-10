package icu.nothingless.controller.config;

import jakarta.servlet.http.HttpSession;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;

/**
 * WebSocket 配置器 - 用于获取 HttpSession
 */
public class ChatConfigurator extends ServerEndpointConfig.Configurator {
    
    public static final String HTTP_SESSION = "HTTP_SESSION";
    
    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        // 获取 HttpSession 并放入 WebSocket 配置
        HttpSession httpSession = (HttpSession) request.getHttpSession();
        sec.getUserProperties().put(HTTP_SESSION, httpSession);
    }
}