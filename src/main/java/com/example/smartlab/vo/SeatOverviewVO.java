package com.example.smartlab.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SeatOverviewVO {

    private Long seatId;
    private String seatCode;
    private String seatName;
    private String seatStatus;
    private String powerStatus;
    private Integer relayChannel;
    private BigDecimal hourlyRate;
    private LocalDateTime currentSessionStartedAt;
    private Long currentUserId;
    private String currentUserCode;
    private String currentUserName;
    private String currentUserPhone;
    private Long relayDeviceId;
    private String relayDeviceCode;
    private String relayDeviceName;
    private String relayDeviceStatus;
}
