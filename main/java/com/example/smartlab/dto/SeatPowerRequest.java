package com.example.smartlab.dto;

import lombok.Data;

@Data
public class SeatPowerRequest {

    private Long userId;
    private String source;
    private String remark;
}
