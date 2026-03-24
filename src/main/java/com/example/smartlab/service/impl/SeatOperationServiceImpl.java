package com.example.smartlab.service.impl;

import com.example.smartlab.dto.FinishSeatSessionRequest;
import com.example.smartlab.dto.SeatPowerRequest;
import com.example.smartlab.entity.IotSeat;
import com.example.smartlab.entity.IotSeatSession;
import com.example.smartlab.service.IotRelayActionService;
import com.example.smartlab.service.IotSeatService;
import com.example.smartlab.service.IotSeatSessionService;
import com.example.smartlab.service.IotSystemLogService;
import com.example.smartlab.service.SeatOperationService;
import com.example.smartlab.service.SeatSessionOperationService;
import com.example.smartlab.service.StreamService;
import com.example.smartlab.vo.StreamEventVO;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeatOperationServiceImpl implements SeatOperationService {

    private final IotSeatService iotSeatService;
    private final IotSeatSessionService iotSeatSessionService;
    private final SeatSessionOperationService seatSessionOperationService;
    private final IotRelayActionService iotRelayActionService;
    private final IotSystemLogService iotSystemLogService;
    private final StreamService streamService;

    public SeatOperationServiceImpl(IotSeatService iotSeatService,
                                    IotSeatSessionService iotSeatSessionService,
                                    SeatSessionOperationService seatSessionOperationService,
                                    IotRelayActionService iotRelayActionService,
                                    IotSystemLogService iotSystemLogService,
                                    StreamService streamService) {
        this.iotSeatService = iotSeatService;
        this.iotSeatSessionService = iotSeatSessionService;
        this.seatSessionOperationService = seatSessionOperationService;
        this.iotRelayActionService = iotRelayActionService;
        this.iotSystemLogService = iotSystemLogService;
        this.streamService = streamService;
    }

    @Override
    @Transactional
    public IotSeat powerOn(Long seatId, SeatPowerRequest request) {
        IotSeat seat = requireSeat(seatId);
        seat.setPowerStatus("on");
        if (seat.getCurrentUserId() != null) {
            seat.setSeatStatus("occupied");
        }
        iotSeatService.updateById(seat);

        String source = request == null ? "manual_control" : defaultText(request.getSource(), "manual_control");
        String remark = request == null ? "" : defaultText(request.getRemark(), "");
        iotRelayActionService.recordAction(seat.getId(), seat.getRelayDeviceId(), seat.getRelayChannel(),
                "power_on", source, "success", remark);
        iotSystemLogService.recordLog(seat.getRelayDeviceId(), seat.getId(), request == null ? null : request.getUserId(),
                "GPIO", "info", "Seat power on",
                "{\"action\":\"power_on\",\"source\":\"" + source + "\"}");
        streamService.publish(new StreamEventVO("seat-powered-on", seat, LocalDateTime.now()));
        return seat;
    }

    @Override
    @Transactional
    public IotSeat powerOff(Long seatId, SeatPowerRequest request) {
        IotSeat seat = requireSeat(seatId);
        IotSeatSession activeSession = iotSeatSessionService.getActiveSessionBySeatId(seatId);
        if (activeSession != null) {
            FinishSeatSessionRequest finishRequest = new FinishSeatSessionRequest();
            finishRequest.setActionSource(request == null ? "manual_control" : defaultText(request.getSource(), "manual_control"));
            finishRequest.setRemark(request == null ? "Session finished by power off" : defaultText(request.getRemark(), "Session finished by power off"));
            seatSessionOperationService.finishSession(activeSession.getId(), finishRequest);
            seat = requireSeat(seatId);
        }

        seat.setPowerStatus("off");
        if (seat.getCurrentUserId() == null) {
            seat.setSeatStatus("idle");
        }
        iotSeatService.updateById(seat);

        String source = request == null ? "manual_control" : defaultText(request.getSource(), "manual_control");
        String remark = request == null ? "" : defaultText(request.getRemark(), "");
        iotRelayActionService.recordAction(seat.getId(), seat.getRelayDeviceId(), seat.getRelayChannel(),
                "power_off", source, "success", remark);
        iotSystemLogService.recordLog(seat.getRelayDeviceId(), seat.getId(), request == null ? null : request.getUserId(),
                "GPIO", "info", "Seat power off",
                "{\"action\":\"power_off\",\"source\":\"" + source + "\"}");
        streamService.publish(new StreamEventVO("seat-powered-off", seat, LocalDateTime.now()));
        return seat;
    }

    private IotSeat requireSeat(Long seatId) {
        IotSeat seat = iotSeatService.getById(seatId);
        if (seat == null) {
            throw new IllegalArgumentException("Seat not found: " + seatId);
        }
        return seat;
    }

    private String defaultText(String text, String fallback) {
        return text == null || text.isBlank() ? fallback : text;
    }
}
