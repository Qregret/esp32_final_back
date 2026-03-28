package com.example.smartlab.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.smartlab.dto.EnvironmentReadingCreateRequest;
import com.example.smartlab.entity.IotDevice;
import com.example.smartlab.entity.IotEnvironmentReading;
import com.example.smartlab.entity.IotSystemLog;
import com.example.smartlab.service.EnvironmentOperationService;
import com.example.smartlab.service.IotDeviceService;
import com.example.smartlab.service.IotEnvironmentReadingService;
import com.example.smartlab.service.IotSystemLogService;
import com.example.smartlab.service.StreamService;
import com.example.smartlab.support.AppTime;
import com.example.smartlab.vo.StreamEventVO;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnvironmentOperationServiceImpl implements EnvironmentOperationService {

    private static final String ENV_LOG_MESSAGE = "环境数据已接收";
    private static final long ENV_LOG_INTERVAL_SECONDS = 60;

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
        reading.setRecordedAt(AppTime.now());
        iotEnvironmentReadingService.save(reading);

        IotDevice device = new IotDevice();
        device.setId(request.getSensorDeviceId());
        device.setLastSeenAt(AppTime.now());
        iotDeviceService.updateById(device);

        String source = request.getSource() == null || request.getSource().isBlank() ? "device_report" : request.getSource();
        if (shouldRecordEnvironmentLog(request.getSensorDeviceId())) {
            iotSystemLogService.recordLog(
                    request.getSensorDeviceId(),
                    null,
                    null,
                    "MQTT",
                    "info",
                    ENV_LOG_MESSAGE,
                    "{\"source\":\"" + source + "\",\"temperatureC\":" + request.getTemperatureC()
                            + ",\"humidityPercent\":" + request.getHumidityPercent() + "}");
        }

        streamService.publish(new StreamEventVO("environment-updated", reading, AppTime.now()));
        return reading;
    }

    private boolean shouldRecordEnvironmentLog(Long deviceId) {
        IotSystemLog lastLog = iotSystemLogService.getOne(new LambdaQueryWrapper<IotSystemLog>()
                .eq(IotSystemLog::getDeviceId, deviceId)
                .eq(IotSystemLog::getLogType, "MQTT")
                .eq(IotSystemLog::getMessage, ENV_LOG_MESSAGE)
                .orderByDesc(IotSystemLog::getCreatedAt)
                .last("LIMIT 1"), false);
        if (lastLog == null || lastLog.getCreatedAt() == null) {
            return true;
        }
        LocalDateTime threshold = AppTime.now().minusSeconds(ENV_LOG_INTERVAL_SECONDS);
        return lastLog.getCreatedAt().isBefore(threshold);
    }
}
