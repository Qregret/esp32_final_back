package com.example.smartlab.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class AuthEventCreateRequest {

    private Long authEventId;
    private String stage;
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
    private String sessionSource;
}
