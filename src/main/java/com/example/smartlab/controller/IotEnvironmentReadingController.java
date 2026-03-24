package com.example.smartlab.controller;

import com.example.smartlab.dto.EnvironmentReadingCreateRequest;
import com.example.smartlab.entity.IotEnvironmentReading;
import com.example.smartlab.service.EnvironmentOperationService;
import com.example.smartlab.service.IotEnvironmentReadingService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/environment")
@RequiredArgsConstructor
public class IotEnvironmentReadingController {

    private final IotEnvironmentReadingService iotEnvironmentReadingService;
    private final EnvironmentOperationService environmentOperationService;

    @GetMapping("/readings")
    public List<IotEnvironmentReading> listAll() {
        return iotEnvironmentReadingService.list();
    }

    @GetMapping("/latest")
    public IotEnvironmentReading getLatest() {
        return iotEnvironmentReadingService.getLatestReading();
    }

    @PostMapping("/readings")
    public IotEnvironmentReading create(@Valid @RequestBody EnvironmentReadingCreateRequest request) {
        return environmentOperationService.createReading(request);
    }
}
