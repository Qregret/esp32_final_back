package com.example.smartlab.service.impl;

import com.example.smartlab.dto.EnvironmentReadingCreateRequest;
import com.example.smartlab.entity.IotDevice;
import com.example.smartlab.entity.IotEnvironmentReading;
import com.example.smartlab.service.EnvironmentOperationService;
import com.example.smartlab.service.IotDeviceService;
import com.example.smartlab.service.IotEnvironmentReadingService;
import com.example.smartlab.service.IotSystemLogService;
import com.example.smartlab.service.StreamService;
import com.example.smartlab.vo.StreamEventVO;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnvironmentOperationServiceImpl implements EnvironmentOperationService {

    private final IotEnvironmentReadingService iotEnvironmentReadingService;
    private final IotDeviceService iotDeviceService;
    private final IotSystemLogService iotSystemLogService;
    private final StreamService streamService;

    public EnvironmentOperationServiceImpl(IotEnvironmentReadingService iotEnvironmentReadingService,
                                           IotDeviceService iotDeviceService,
                                           IotSystemLogService iotSystemLogService,
                                           StreamService streamService) {
        this.iotEnvironmentReadingService = iotEnvironmentReadingService;
        this.iotDeviceService = iotDeviceService;
        this.iotSystemLogService = iotSystemLogService;
        this.streamService = streamService;
    }

    @Override
    @Transactional
    public IotEnvironmentReading createReading(EnvironmentReadingCreateRequest request) {
        IotEnvironmentReading reading = new IotEnvironmentReading();
        reading.setSensorDeviceId(request.getSensorDeviceId());
        reading.setTemperatureC(request.getTemperatureC());
        reading.setHumidityPercent(request.getHumidityPercent());
        reading.setTempGauge(request.getTempGauge());
        reading.setHumidityGauge(request.getHumidityGauge());
        reading.setBatteryPercent(request.getBatteryPercent());
        reading.setRecordedAt(LocalDateTime.now());
        iotEnvironmentReadingService.save(reading);

        IotDevice device = new IotDevice();
        device.setId(request.getSensorDeviceId());
        device.setLastSeenAt(LocalDateTime.now());
        iotDeviceService.updateById(device);

        String source = request.getSource() == null || request.getSource().isBlank() ? "device_report" : request.getSource();
        iotSystemLogService.recordLog(request.getSensorDeviceId(), null, null, "MQTT", "info",
                "Environment reading received",
                "{\"source\":\"" + source + "\",\"temperatureC\":" + request.getTemperatureC()
                        + ",\"humidityPercent\":" + request.getHumidityPercent() + "}");
        streamService.publish(new StreamEventVO("environment-updated", reading, LocalDateTime.now()));
        return reading;
    }
}
