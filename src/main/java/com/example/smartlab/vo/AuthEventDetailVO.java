package com.example.smartlab.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AuthEventDetailVO {

    private Long authEventId;
    private String flowState;
    private String authResult;
    private BigDecimal similarity;
    private String cameraStatus;
    private String statusText;
    private String rfidUid;
    private String snapshotUrl;
    private LocalDateTime createdAt;
    private Long userId;
    private String userCode;
    private String userName;
    private String identityStatus;
    private Long seatId;
    private String seatCode;
    private String seatName;
    private String seatStatus;
    private Long gatewayDeviceId;
    private String gatewayDeviceCode;
    private String gatewayDeviceName;
    private Long cameraDeviceId;
    private String cameraDeviceCode;
    private String cameraDeviceName;
    private Long rfidDeviceId;
    private String rfidDeviceCode;
    private String rfidDeviceName;
}
