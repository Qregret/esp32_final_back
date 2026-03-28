package com.example.smartlab.controller;

import com.example.smartlab.dto.FinishSeatSessionRequest;
import com.example.smartlab.dto.StartSeatSessionRequest;
import com.example.smartlab.entity.IotSeatSession;
import com.example.smartlab.service.IotSeatSessionService;
import com.example.smartlab.service.SeatSessionOperationService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seat-sessions")
@RequiredArgsConstructor
public class IotSeatSessionController {

    private final IotSeatSessionService iotSeatSessionService;
    private final SeatSessionOperationService seatSessionOperationService;

    @GetMapping
    public List<IotSeatSession> listAll() {
        return iotSeatSessionService.list();
    }

    @GetMapping("/active")
    public List<IotSeatSession> listActive() {
        return iotSeatSessionService.listActiveSessions();
    }

    @PostMapping("/start")
    public IotSeatSession start(@Valid @RequestBody StartSeatSessionRequest request) {
        return seatSessionOperationService.startSession(request);
    }

    @PostMapping("/{sessionId}/finish")
    public IotSeatSession finish(@PathVariable Long sessionId,
                                 @RequestBody(required = false) FinishSeatSessionRequest request) {
        return seatSessionOperationService.finishSession(sessionId, request);
    }
}
