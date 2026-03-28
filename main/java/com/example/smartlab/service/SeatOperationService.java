package com.example.smartlab.service;

import com.example.smartlab.dto.SeatStateSyncRequest;
import com.example.smartlab.dto.SeatPowerRequest;
import com.example.smartlab.entity.IotSeat;
import java.util.List;

public interface SeatOperationService {

    IotSeat powerOn(Long seatId, SeatPowerRequest request);

    IotSeat powerOff(Long seatId, SeatPowerRequest request);

    List<IotSeat> syncStates(SeatStateSyncRequest request);
}
