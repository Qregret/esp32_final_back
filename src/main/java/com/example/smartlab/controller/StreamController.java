package com.example.smartlab.controller;

import com.example.smartlab.service.StreamService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/stream")
@RequiredArgsConstructor
public class StreamController {

    private final StreamService streamService;

    @GetMapping("/events")
    public SseEmitter subscribe() {
        return streamService.subscribe();
    }
}
