package com.example.smartlab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("iot_seats")
public class IotSeat {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String seatCode;
    private String seatName;
    private Long relayDeviceId;
    private Integer relayChannel;
    private String seatStatus;
    private String powerStatus;
    private Long currentUserId;
    private BigDecimal hourlyRate;
    private BigDecimal dailyCapAmount;
    private LocalDateTime currentSessionStartedAt;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
