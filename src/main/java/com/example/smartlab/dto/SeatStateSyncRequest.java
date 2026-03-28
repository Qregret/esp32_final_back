package com.example.smartlab.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SeatStateSyncRequest {

    private String deviceCode;
    private String source;
    private String remark;

    @JsonAlias({"states", "seatStates"})
    private List<SeatStateSyncItem> seats = new ArrayList<>();
}
