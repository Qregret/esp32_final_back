package com.example.smartlab.controller;

import com.example.smartlab.service.DashboardService;
import com.example.smartlab.service.IotSeatService;
import com.example.smartlab.service.IotAuthEventService;
import com.example.smartlab.vo.DashboardOverviewVO;
import com.example.smartlab.vo.SeatOverviewVO;
import com.example.smartlab.vo.AuthEventDetailVO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final IotSeatService iotSeatService;
    private final IotAuthEventService iotAuthEventService;
    private final DashboardService dashboardService;

    @GetMapping("/seat-user-device")
    public List<SeatOverviewVO> seatUserDeviceOverview() {
        return iotSeatService.listSeatOverview();
    }

    @GetMapping("/seat-user-device-auth")
    public List<AuthEventDetailVO> seatUserDeviceAuthOverview(@RequestParam(required = false) String authResult) {
        return iotAuthEventService.listAuthEventDetails(authResult);
    }

    @GetMapping("/overview")
    public DashboardOverviewVO overview() {
        return dashboardService.getOverview();
    }
}
