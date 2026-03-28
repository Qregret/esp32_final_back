package com.example.smartlab.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class EnvironmentReadingCreateRequest {

    @NotNull
    private Long sensorDeviceId;

    @NotNull
    private BigDecimal temperatureC;

    @NotNull
    private BigDecimal humidityPercent;

    private Integer tempGauge;
    private Integer humidityGauge;
    private Integer batteryPercent;
    private String source;
}
