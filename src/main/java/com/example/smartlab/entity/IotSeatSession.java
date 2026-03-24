package com.example.smartlab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("iot_seat_sessions")
public class IotSeatSession {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long seatId;
    private Long userId;
    private String sessionSource;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Integer durationSeconds;
    private Integer billingHours;
    private BigDecimal hourlyRate;
    private BigDecimal chargeAmount;
    private String sessionStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
