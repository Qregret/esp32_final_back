package com.example.smartlab.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.smartlab.dto.FinishSeatSessionRequest;
import com.example.smartlab.dto.StartSeatSessionRequest;
import com.example.smartlab.entity.IotSeat;
import com.example.smartlab.entity.IotSeatSession;
import com.example.smartlab.service.IotSeatService;
import com.example.smartlab.service.IotSeatSessionService;
import com.example.smartlab.service.IotSystemLogService;
import com.example.smartlab.service.SeatSessionOperationService;
import com.example.smartlab.service.StreamService;
import com.example.smartlab.vo.StreamEventVO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeatSessionOperationServiceImpl implements SeatSessionOperationService {

    private final IotSeatService iotSeatService;
    private final IotSeatSessionService iotSeatSessionService;
    private final IotSystemLogService iotSystemLogService;
    private final StreamService streamService;

    public SeatSessionOperationServiceImpl(IotSeatService iotSeatService,
                                           IotSeatSessionService iotSeatSessionService,
                                           IotSystemLogService iotSystemLogService,
                                           StreamService streamService) {
        this.iotSeatService = iotSeatService;
        this.iotSeatSessionService = iotSeatSessionService;
        this.iotSystemLogService = iotSystemLogService;
        this.streamService = streamService;
    }

    @Override
    @Transactional
    public IotSeatSession startSession(StartSeatSessionRequest request) {
        IotSeat seat = requireSeat(request.getSeatId());
        if ("occupied".equalsIgnoreCase(seat.getSeatStatus())) {
            throw new IllegalStateException("Seat is already occupied.");
        }
        if (iotSeatSessionService.getActiveSessionBySeatId(seat.getId()) != null) {
            throw new IllegalStateException("Seat already has an active session.");
        }

        LocalDateTime now = LocalDateTime.now();
        IotSeatSession session = new IotSeatSession();
        session.setSeatId(seat.getId());
        session.setUserId(request.getUserId());
        session.setSessionSource(defaultText(request.getSessionSource(), "manual"));
        session.setStartedAt(now);
        session.setDurationSeconds(0);
        session.setBillingHours(1);
        session.setHourlyRate(defaultRate(seat.getHourlyRate()));
        session.setChargeAmount(defaultRate(seat.getHourlyRate()));
        session.setSessionStatus("active");
        iotSeatSessionService.save(session);

        seat.setCurrentUserId(request.getUserId());
        seat.setCurrentSessionStartedAt(now);
        seat.setSeatStatus("occupied");
        if (!"on".equalsIgnoreCase(seat.getPowerStatus())) {
            seat.setPowerStatus("on");
        }
        iotSeatService.updateById(seat);

        iotSystemLogService.recordLog(seat.getRelayDeviceId(), seat.getId(), request.getUserId(),
                "SESSION", "info", "Seat session started",
                "{\"source\":\"" + session.getSessionSource() + "\"}");
        streamService.publish(new StreamEventVO("seat-session-started", session, now));
        return session;
    }

    @Override
    @Transactional
    public IotSeatSession finishSession(Long sessionId, FinishSeatSessionRequest request) {
        IotSeatSession session = iotSeatSessionService.getById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }
        if (!"active".equalsIgnoreCase(session.getSessionStatus())) {
            return session;
        }

        LocalDateTime now = LocalDateTime.now();
        int durationSeconds = Math.max((int) Duration.between(session.getStartedAt(), now).getSeconds(), 0);
        int billingHours = Math.max(1, (int) Math.ceil(durationSeconds / 3600.0));
        BigDecimal hourlyRate = defaultRate(session.getHourlyRate());
        BigDecimal chargeAmount = hourlyRate.multiply(BigDecimal.valueOf(billingHours)).setScale(2, RoundingMode.HALF_UP);

        session.setEndedAt(now);
        session.setDurationSeconds(durationSeconds);
        session.setBillingHours(billingHours);
        session.setChargeAmount(chargeAmount);
        session.setSessionStatus("finished");
        iotSeatSessionService.updateById(session);

        IotSeat seat = requireSeat(session.getSeatId());
        iotSeatService.update(new LambdaUpdateWrapper<IotSeat>()
                .eq(IotSeat::getId, seat.getId())
                .set(IotSeat::getCurrentUserId, null)
                .set(IotSeat::getCurrentSessionStartedAt, null)
                .set(IotSeat::getSeatStatus, "idle"));
        seat.setCurrentUserId(null);
        seat.setCurrentSessionStartedAt(null);
        seat.setSeatStatus("idle");

        String source = request == null ? "manual" : defaultText(request.getActionSource(), "manual");
        String remark = request == null ? "" : defaultText(request.getRemark(), "");
        iotSystemLogService.recordLog(seat.getRelayDeviceId(), seat.getId(), session.getUserId(),
                "SESSION", "info", "Seat session finished",
                "{\"source\":\"" + source + "\",\"remark\":\"" + remark + "\"}");
        streamService.publish(new StreamEventVO("seat-session-finished", session, now));
        return session;
    }

    private IotSeat requireSeat(Long seatId) {
        IotSeat seat = iotSeatService.getById(seatId);
        if (seat == null) {
            throw new IllegalArgumentException("Seat not found: " + seatId);
        }
        return seat;
    }

    private BigDecimal defaultRate(BigDecimal rate) {
        return rate == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : rate.setScale(2, RoundingMode.HALF_UP);
    }

    private String defaultText(String text, String fallback) {
        return text == null || text.isBlank() ? fallback : text;
    }
}
