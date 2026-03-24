package com.example.smartlab.controller;

import com.example.smartlab.dto.AuthEventCreateRequest;
import com.example.smartlab.entity.IotAuthEvent;
import com.example.smartlab.service.AuthWorkflowService;
import com.example.smartlab.service.IotAuthEventService;
import com.example.smartlab.vo.AuthEventDetailVO;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth-events")
@RequiredArgsConstructor
public class IotAuthEventController {

    private final IotAuthEventService iotAuthEventService;
    private final AuthWorkflowService authWorkflowService;

    @GetMapping
    public List<IotAuthEvent> listAll() {
        return iotAuthEventService.list();
    }

    @GetMapping("/latest")
    public List<IotAuthEvent> listLatest(@RequestParam(defaultValue = "10") int limit) {
        return iotAuthEventService.listLatest(limit);
    }

    @GetMapping("/granted")
    public List<IotAuthEvent> listGranted() {
        return iotAuthEventService.listGrantedEvents();
    }

    @GetMapping("/details")
    public List<AuthEventDetailVO> listDetails(@RequestParam(required = false) String authResult) {
        return iotAuthEventService.listAuthEventDetails(authResult);
    }

    @PostMapping
    public IotAuthEvent create(@Valid @RequestBody AuthEventCreateRequest request) {
        return authWorkflowService.handle(request);
    }

    @PostMapping("/rfid-scan")
    public IotAuthEvent rfidScan(@RequestBody AuthEventCreateRequest request) {
        request.setStage("rfid_scan");
        request.setFlowState("processing");
        request.setAuthResult("standby");
        request.setStatusText(request.getStatusText() == null ? "RFID scanned, waiting for camera and AI." : request.getStatusText());
        return authWorkflowService.handle(request);
    }

    @PostMapping("/camera-upload")
    public IotAuthEvent cameraUpload(@RequestBody AuthEventCreateRequest request) {
        request.setStage("camera_upload");
        request.setFlowState("processing");
        request.setStatusText(request.getStatusText() == null ? "Camera image uploaded, waiting for AI result." : request.getStatusText());
        return authWorkflowService.handle(request);
    }

    @PostMapping("/ai-result")
    public IotAuthEvent aiResult(@RequestBody AuthEventCreateRequest request) {
        request.setStage("ai_result");
        request.setFlowState("processing");
        request.setStatusText(request.getStatusText() == null ? "AI result received, waiting for final decision." : request.getStatusText());
        return authWorkflowService.handle(request);
    }

    @PostMapping("/finalize")
    public IotAuthEvent finalizeAuth(@RequestBody AuthEventCreateRequest request) {
        request.setStage("finalize");
        return authWorkflowService.handle(request);
    }
}
