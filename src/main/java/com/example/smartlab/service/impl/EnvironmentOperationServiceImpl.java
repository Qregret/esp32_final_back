package com.example.smartlab.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.smartlab.dto.EnvironmentReadingCreateRequest;
import com.example.smartlab.entity.IotDevice;
import com.example.smartlab.entity.IotEnvironmentReading;
import com.example.smartlab.service.EnvironmentOperationService;
import com.example.smartlab.service.IotDeviceService;
import com.example.smartlab.service.IotEnvironmentReadingService;
import com.example.smartlab.service.StreamService;
import com.example.smartlab.support.AppTime;
import com.example.smartlab.vo.StreamEventVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnvironmentOperationServiceImpl implements EnvironmentOperationService {

    private final IotEnvironmentReadingService iotEnvironmentReadingService;
    private final IotDeviceService iotDeviceService;
    private final StreamService streamService;

    public EnvironmentOperationServiceImpl(IotEnvironmentReadingService iotEnvironmentReadingService,
                                           IotDeviceService iotDeviceService,
                                           StreamService streamService) {
        this.iotEnvironmentReadingService = iotEnvironmentReadingService;
        this.iotDeviceService = iotDeviceService;
        this.streamService = streamService;
    }

    @Override
    @Transactional
    public IotEnvironmentReading createReading(EnvironmentReadingCreateRequest request) {
        IotEnvironmentReading reading = iotEnvironmentReadingService.getOne(
                new LambdaQueryWrapper<IotEnvironmentReading>()
                        .eq(IotEnvironmentReading::getSensorDeviceId, request.getSensorDeviceId())
                        .orderByDesc(IotEnvironmentReading::getRecordedAt)
                        .last("LIMIT 1"),
                false);

        if (reading == null) {
            reading = new IotEnvironmentReading();
            reading.setSensorDeviceId(request.getSensorDeviceId());
            reading.setCreatedAt(AppTime.now());
        }

        reading.setTemperatureC(request.getTemperatureC());
        reading.setHumidityPercent(request.getHumidityPercent());
        reading.setTempGauge(request.getTempGauge());
        reading.setHumidityGauge(request.getHumidityGauge());
        reading.setBatteryPercent(request.getBatteryPercent());
        reading.setRecordedAt(AppTime.now());

        if (reading.getId() == null) {
            iotEnvironmentReadingService.save(reading);
        } else {
            iotEnvironmentReadingService.updateById(reading);
            iotEnvironmentReadingService.remove(
                    new LambdaQueryWrapper<IotEnvironmentReading>()
                            .eq(IotEnvironmentReading::getSensorDeviceId, request.getSensorDeviceId())
                            .ne(IotEnvironmentReading::getId, reading.getId()));
        }

        IotDevice device = new IotDevice();
        device.setId(request.getSensorDeviceId());
        device.setLastSeenAt(AppTime.now());
        iotDeviceService.updateById(device);

        streamService.publish(new StreamEventVO("environment-updated", reading, AppTime.now()));
        return reading;
    }
}
