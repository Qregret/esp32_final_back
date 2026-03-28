package com.example.smartlab.vo;

import com.example.smartlab.entity.IotEnvironmentReading;
import com.example.smartlab.entity.IotSystemLog;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class DashboardOverviewVO {

    private LocalDateTime serverTime;

    private AuthEventDetailVO auth;
    private AuthEventDetailVO currentAuth;

    private List<SeatOverviewVO> seats;
    private List<SeatOverviewVO> currentSeats;

    private IotEnvironmentReading environment;
    private IotEnvironmentReading latestEnvironment;

    private List<IotSystemLog> logs;
    private List<IotSystemLog> recentLogs;
}
