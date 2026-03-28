package com.example.smartlab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("iot_auth_events")
public class IotAuthEvent {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long gatewayDeviceId;
    private Long cameraDeviceId;
    private Long rfidDeviceId;
    private Long userId;
    private Long seatId;
    private String rfidUid;
    private String cameraStatus;
    private String flowState;
    private BigDecimal similarity;
    private String authResult;
    private String statusText;
    private String snapshotUrl;
    private LocalDateTime createdAt;
}
