package com.example.smartlab.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StartSeatSessionRequest {

    @NotNull
    private Long seatId;

    @NotNull
    private Long userId;

    private String sessionSource;
}
