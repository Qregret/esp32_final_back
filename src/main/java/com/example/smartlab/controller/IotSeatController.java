package com.example.smartlab.controller;

import com.example.smartlab.dto.SeatPowerRequest;
import com.example.smartlab.entity.IotSeat;
import com.example.smartlab.service.IotSeatService;
import com.example.smartlab.service.SeatOperationService;
import com.example.smartlab.vo.SeatOverviewVO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class IotSeatController {

    private final IotSeatService iotSeatService;
    private final SeatOperationService seatOperationService;

    @GetMapping
    public List<IotSeat> listAll() {
        return iotSeatService.list();
    }

    @GetMapping("/occupied")
    public List<IotSeat> listOccupied() {
        return iotSeatService.listOccupiedSeats();
    }

    @GetMapping("/user/{userId}")
    public List<IotSeat> listByCurrentUser(@PathVariable Long userId) {
        return iotSeatService.listByCurrentUserId(userId);
    }

    @GetMapping("/overview")
    public List<SeatOverviewVO> seatOverview() {
        return iotSeatService.listSeatOverview();
    }

    @GetMapping("/current")
    public List<SeatOverviewVO> currentSeats() {
        return iotSeatService.listSeatOverview();
    }

    @PostMapping("/{seatId}/power-on")
    public IotSeat powerOn(@PathVariable Long seatId, @RequestBody(required = false) SeatPowerRequest request) {
        return seatOperationService.powerOn(seatId, request);
    }

    @PostMapping("/{seatId}/power-off")
    public IotSeat powerOff(@PathVariable Long seatId, @RequestBody(required = false) SeatPowerRequest request) {
        return seatOperationService.powerOff(seatId, request);
    }
}
