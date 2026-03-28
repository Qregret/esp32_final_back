package com.example.smartlab.service;

import com.example.smartlab.vo.StreamEventVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface StreamService {

    SseEmitter subscribe();

    void publish(StreamEventVO event);
}
