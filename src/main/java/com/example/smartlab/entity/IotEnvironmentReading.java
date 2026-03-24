package com.example.smartlab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("iot_environment_readings")
public class IotEnvironmentReading {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sensorDeviceId;
    private BigDecimal temperatureC;
    private BigDecimal humidityPercent;
    private Integer tempGauge;
    private Integer humidityGauge;
    private Integer batteryPercent;
    private LocalDateTime recordedAt;
    private LocalDateTime createdAt;
}
