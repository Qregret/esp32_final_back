package com.example.smartlab.service.impl;

import com.example.smartlab.service.WebSocketStreamService;
import com.example.smartlab.vo.StreamEventVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Service
public class WebSocketStreamServiceImpl implements WebSocketStreamService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketStreamServiceImpl.class);

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final ObjectMapper objectMapper;

    public WebSocketStreamServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void register(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void unregister(WebSocketSession session) {
        sessions.remove(session);
    }

    @Override
    public void broadcast(StreamEventVO event) {
        String json;
        try {
            json = objectMapper.writeValueAsString(event);
        } catch (Exception exception) {
            log.warn("WebSocket event serialization failed: {}", exception.getMessage());
            return;
        }

        for (WebSocketSession session : sessions) {
            if (!session.isOpen()) {
                sessions.remove(session);
                continue;
            }
            try {
                session.sendMessage(new TextMessage(json));
            } catch (IOException exception) {
                sessions.remove(session);
                try {
                    session.close();
                } catch (IOException closeException) {
                    log.debug("WebSocket session close failed: {}", closeException.getMessage());
                }
            }
        }
    }
}
