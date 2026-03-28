package com.example.smartlab.service;

import com.example.smartlab.vo.StreamEventVO;
import org.springframework.web.socket.WebSocketSession;

public interface WebSocketStreamService {

    void register(WebSocketSession session);

    void unregister(WebSocketSession session);

    void broadcast(StreamEventVO event);
}
