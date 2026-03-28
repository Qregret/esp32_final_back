package com.example.smartlab.service.impl;

import com.example.smartlab.service.StreamService;
import com.example.smartlab.service.WebSocketStreamService;
import com.example.smartlab.vo.StreamEventVO;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class StreamServiceImpl implements StreamService {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final WebSocketStreamService webSocketStreamService;

    public StreamServiceImpl(WebSocketStreamService webSocketStreamService) {
        this.webSocketStreamService = webSocketStreamService;
    }

    @Override
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(ex -> emitters.remove(emitter));
        return emitter;
    }

    @Override
    public void publish(StreamEventVO event) {
        webSocketStreamService.broadcast(event);
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(event.getEventType()).data(event));
            } catch (IOException ex) {
                emitter.complete();
                emitters.remove(emitter);
            }
        }
    }
}
