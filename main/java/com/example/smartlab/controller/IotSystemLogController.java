package com.example.smartlab.controller;

import com.example.smartlab.entity.IotSystemLog;
import com.example.smartlab.service.IotSystemLogService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system-logs")
@RequiredArgsConstructor
public class IotSystemLogController {

    private final IotSystemLogService iotSystemLogService;

    @GetMapping
    public List<IotSystemLog> listAll() {
        return iotSystemLogService.list();
    }

    @GetMapping("/latest")
    public List<IotSystemLog> listLatest(@RequestParam(defaultValue = "10") int limit) {
        return iotSystemLogService.listLatest(limit);
    }

    @GetMapping("/recent")
    public List<IotSystemLog> listRecent(@RequestParam(defaultValue = "10") int limit) {
        return iotSystemLogService.listLatest(limit);
    }
}
