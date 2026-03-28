package com.example.smartlab.controller;

import com.example.smartlab.entity.IotDevice;
import com.example.smartlab.service.IotDeviceService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class IotDeviceController {

    private final IotDeviceService iotDeviceService;

    @GetMapping
    public List<IotDevice> listAll() {
        return iotDeviceService.list();
    }

    @GetMapping("/online")
    public List<IotDevice> listOnline() {
        return iotDeviceService.listOnlineDevices();
    }

    @GetMapping("/type/{deviceType}")
    public List<IotDevice> listByType(@PathVariable String deviceType) {
        return iotDeviceService.listByDeviceType(deviceType);
    }
}
