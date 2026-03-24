package com.example.smartlab.service;

import com.example.smartlab.dto.SeatPowerRequest;
import com.example.smartlab.entity.IotSeat;

public interface SeatOperationService {

    IotSeat powerOn(Long seatId, SeatPowerRequest request);

    IotSeat powerOff(Long seatId, SeatPowerRequest request);
}
