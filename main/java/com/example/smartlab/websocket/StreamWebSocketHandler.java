package com.example.smartlab.websocket;

import com.example.smartlab.service.WebSocketStreamService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class StreamWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketStreamService webSocketStreamService;

    public StreamWebSocketHandler(WebSocketStreamService webSocketStreamService) {
        this.webSocketStreamService = webSocketStreamService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        webSocketStreamService.register(session);
        session.sendMessage(new TextMessage("{\"eventType\":\"connected\"}"));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        webSocketStreamService.unregister(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        webSocketStreamService.unregister(session);
        if (session.isOpen()) {
            session.close();
        }
    }
}
