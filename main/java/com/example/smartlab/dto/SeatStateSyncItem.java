package com.example.smartlab.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SeatStateSyncItem {

    private Long seatId;

    @JsonAlias({"seatNo", "seat_code"})
    private String seatCode;

    private Boolean occupied;

    @JsonAlias("powerOn")
    private Boolean powerOn;

    public boolean resolveOccupied() {
        if (occupied != null) {
            return occupied;
        }
        return Boolean.TRUE.equals(powerOn);
    }
}
