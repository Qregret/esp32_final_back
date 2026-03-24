package com.example.smartlab.service.impl;

import com.example.smartlab.service.DashboardService;
import com.example.smartlab.service.IotAuthEventService;
import com.example.smartlab.service.IotEnvironmentReadingService;
import com.example.smartlab.service.IotSeatService;
import com.example.smartlab.service.IotSystemLogService;
import com.example.smartlab.vo.DashboardOverviewVO;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final IotAuthEventService iotAuthEventService;
    private final IotSeatService iotSeatService;
    private final IotEnvironmentReadingService iotEnvironmentReadingService;
    private final IotSystemLogService iotSystemLogService;

    public DashboardServiceImpl(IotAuthEventService iotAuthEventService,
                                IotSeatService iotSeatService,
                                IotEnvironmentReadingService iotEnvironmentReadingService,
                                IotSystemLogService iotSystemLogService) {
        this.iotAuthEventService = iotAuthEventService;
        this.iotSeatService = iotSeatService;
        this.iotEnvironmentReadingService = iotEnvironmentReadingService;
        this.iotSystemLogService = iotSystemLogService;
    }

    @Override
    public DashboardOverviewVO getOverview() {
        List<com.example.smartlab.vo.SeatOverviewVO> seats = iotSeatService.listSeatOverview();
        com.example.smartlab.vo.AuthEventDetailVO auth = iotAuthEventService.getLatestDetail();
        com.example.smartlab.entity.IotEnvironmentReading environment = iotEnvironmentReadingService.getLatestReading();
        List<com.example.smartlab.entity.IotSystemLog> logs = iotSystemLogService.listLatest(20);

        DashboardOverviewVO overview = new DashboardOverviewVO();
        overview.setServerTime(LocalDateTime.now());
        overview.setAuth(auth);
        overview.setCurrentAuth(auth);
        overview.setSeats(seats);
        overview.setCurrentSeats(seats);
        overview.setEnvironment(environment);
        overview.setLatestEnvironment(environment);
        overview.setLogs(logs);
        overview.setRecentLogs(logs);
        return overview;
    }
}
