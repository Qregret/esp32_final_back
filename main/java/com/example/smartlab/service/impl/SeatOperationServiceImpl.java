package com.example.smartlab.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.smartlab.dto.FinishSeatSessionRequest;
import com.example.smartlab.dto.SeatPowerRequest;
import com.example.smartlab.dto.SeatStateSyncItem;
import com.example.smartlab.dto.SeatStateSyncRequest;
import com.example.smartlab.entity.IotSeat;
import com.example.smartlab.entity.IotSeatSession;
import com.example.smartlab.entity.IotUser;
import com.example.smartlab.service.IotRelayActionService;
import com.example.smartlab.service.IotSeatService;
import com.example.smartlab.service.IotSeatSessionService;
import com.example.smartlab.service.IotSystemLogService;
import com.example.smartlab.service.IotUserService;
import com.example.smartlab.service.MqttDeviceCommandService;
import com.example.smartlab.service.SeatOperationService;
import com.example.smartlab.service.SeatSessionOperationService;
import com.example.smartlab.support.AppTime;
import com.example.smartlab.service.StreamService;
import com.example.smartlab.vo.StreamEventVO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeatOperationServiceImpl implements SeatOperationService {

    private static final String TEMP_USER_CODE = "V0001";
    private static final String TEMP_USER_NAME = "\u4e34\u65f6\u4f7f\u7528\u8005";

    private final IotSeatService iotSeatService;
    private final IotSeatSessionService iotSeatSessionService;
    private final SeatSessionOperationService seatSessionOperationService;
    private final IotRelayActionService iotRelayActionService;
    private final IotSystemLogService iotSystemLogService;
    private final StreamService streamService;
    private final MqttDeviceCommandService mqttDeviceCommandService;
    private final IotUserService iotUserService;

    public SeatOperationServiceImpl(IotSeatService iotSeatService,
                                    IotSeatSessionService iotSeatSessionService,
                                    SeatSessionOperationService seatSessionOperationService,
                                    IotRelayActionService iotRelayActionService,
                                    IotSystemLogService iotSystemLogService,
                                    StreamService streamService,
                                    MqttDeviceCommandService mqttDeviceCommandService,
                                    IotUserService iotUserService) {
        this.iotSeatService = iotSeatService;
        this.iotSeatSessionService = iotSeatSessionService;
        this.seatSessionOperationService = seatSessionOperationService;
        this.iotRelayActionService = iotRelayActionService;
        this.iotSystemLogService = iotSystemLogService;
        this.streamService = streamService;
        this.mqttDeviceCommandService = mqttDeviceCommandService;
        this.iotUserService = iotUserService;
    }

    @Override
    @Transactional
    public IotSeat powerOn(Long seatId, SeatPowerRequest request) {
        IotSeat seat = requireSeat(seatId);
        String source = request == null ? "manual_control" : defaultText(request.getSource(), "manual_control");
        String remark = request == null ? "" : defaultText(request.getRemark(), "");
        IotUser currentUser = resolveSeatUser(seat);
        if (currentUser == null) {
            currentUser = resolveTemporaryUser();
        }

        LocalDateTime startedAt = seat.getCurrentSessionStartedAt() == null
                ? AppTime.now()
                : seat.getCurrentSessionStartedAt();

        seat.setPowerStatus("on");
        seat.setSeatStatus("occupied");
        seat.setCurrentUserId(currentUser.getId());
        seat.setCurrentSessionStartedAt(startedAt);
        iotSeatService.updateById(seat);

        ensureActiveSession(seat, currentUser.getId(), source, startedAt);

        iotRelayActionService.recordAction(seat.getId(), seat.getRelayDeviceId(), seat.getRelayChannel(),
                "power_on", source, "success", remark);

        iotSystemLogService.recordLog(
                seat.getRelayDeviceId(),
                seat.getId(),
                currentUser.getId(),
                "GPIO",
                "info",
                resolveSeatLabel(seat) + "\u5f00\u542f\uff0c\u4f7f\u7528\u8005\uff1a" + currentUser.getFullName(),
                "{\"action\":\"power_on\",\"source\":\"" + source + "\",\"userName\":\"" + currentUser.getFullName()
                        + "\",\"startedAt\":\"" + startedAt + "\"}");

        streamService.publish(new StreamEventVO("seat-powered-on", seat, AppTime.now()));
        mqttDeviceCommandService.publishSeatPowerCommand(seat, true, source, remark);
        return seat;
    }

    @Override
    @Transactional
    public IotSeat powerOff(Long seatId, SeatPowerRequest request) {
        IotSeat seat = requireSeat(seatId);
        String source = request == null ? "manual_control" : defaultText(request.getSource(), "manual_control");
        String remark = request == null ? "" : defaultText(request.getRemark(), "");
        IotUser currentUser = resolveSeatUser(seat);
        if (currentUser == null && seat.getCurrentSessionStartedAt() != null) {
            currentUser = resolveTemporaryUser();
        }

        IotSeatSession activeSession = iotSeatSessionService.getActiveSessionBySeatId(seatId);
        BigDecimal chargeAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (activeSession != null) {
            FinishSeatSessionRequest finishRequest = new FinishSeatSessionRequest();
            finishRequest.setActionSource(source);
            finishRequest.setRemark(defaultText(remark, "\u8bbe\u5907\u5173\u95ed\uff0c\u81ea\u52a8\u7ed3\u7b97"));
            IotSeatSession finishedSession = seatSessionOperationService.finishSession(activeSession.getId(), finishRequest);
            chargeAmount = defaultRate(finishedSession.getChargeAmount());
            seat = requireSeat(seatId);
        } else if (seat.getCurrentSessionStartedAt() != null) {
            chargeAmount = calculateChargeAmount(seat.getCurrentSessionStartedAt(), seat.getHourlyRate());
        }

        seat.setPowerStatus("off");
        seat.setSeatStatus("idle");
        seat.setCurrentUserId(null);
        seat.setCurrentSessionStartedAt(null);
        iotSeatService.updateById(seat);

        iotRelayActionService.recordAction(seat.getId(), seat.getRelayDeviceId(), seat.getRelayChannel(),
                "power_off", source, "success", remark);

        iotSystemLogService.recordLog(
                seat.getRelayDeviceId(),
                seat.getId(),
                request == null ? null : request.getUserId(),
                "GPIO",
                "info",
                resolveSeatLabel(seat) + "\u5173\u95ed\uff0c\u4f7f\u7528\u8005\uff1a" + resolveUserName(currentUser)
                        + "\uff0c\u6d88\u8d39\u91d1\u989d\uff1a" + formatAmount(chargeAmount) + "\u5143",
                "{\"action\":\"power_off\",\"source\":\"" + source + "\",\"userName\":\"" + resolveUserName(currentUser)
                        + "\",\"chargeAmount\":\"" + formatAmount(chargeAmount) + "\"}");

        streamService.publish(new StreamEventVO("seat-powered-off", seat, AppTime.now()));
        mqttDeviceCommandService.publishSeatPowerCommand(seat, false, source, remark);
        return seat;
    }

    @Override
    @Transactional
    public List<IotSeat> syncStates(SeatStateSyncRequest request) {
        if (request == null || request.getSeats() == null || request.getSeats().isEmpty()) {
            throw new IllegalArgumentException("Seat state payload is empty");
        }

        String source = defaultText(request.getSource(), "mqtt");
        String remark = defaultText(request.getRemark(), "");
        String deviceCode = defaultText(request.getDeviceCode(), "");
        List<IotSeat> updatedSeats = new ArrayList<>();

        for (SeatStateSyncItem item : request.getSeats()) {
            IotSeat seat = requireSeat(item);
            boolean occupied = item.resolveOccupied();
            String targetPowerStatus = occupied ? "on" : "off";
            String targetSeatStatus = occupied ? "occupied" : "idle";
            boolean changed = !targetPowerStatus.equalsIgnoreCase(defaultText(seat.getPowerStatus(), ""))
                    || !targetSeatStatus.equalsIgnoreCase(defaultText(seat.getSeatStatus(), ""));

            IotUser currentUser = resolveSeatUser(seat);
            BigDecimal chargeAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

            if (occupied) {
                if (currentUser == null) {
                    currentUser = resolveTemporaryUser();
                }
                LocalDateTime startedAt = seat.getCurrentSessionStartedAt() == null
                        ? AppTime.now()
                        : seat.getCurrentSessionStartedAt();
                seat.setPowerStatus("on");
                seat.setSeatStatus("occupied");
                seat.setCurrentUserId(currentUser.getId());
                seat.setCurrentSessionStartedAt(startedAt);
                iotSeatService.updateById(seat);
                ensureActiveSession(seat, currentUser.getId(), source, startedAt);
            } else {
                if (currentUser == null && seat.getCurrentSessionStartedAt() != null) {
                    currentUser = resolveTemporaryUser();
                }
                IotSeatSession activeSession = iotSeatSessionService.getActiveSessionBySeatId(seat.getId());
                if (activeSession != null) {
                    FinishSeatSessionRequest finishRequest = new FinishSeatSessionRequest();
                    finishRequest.setActionSource(source);
                    finishRequest.setRemark(defaultText(remark, "\u8bbe\u5907\u5173\u95ed\uff0c\u81ea\u52a8\u7ed3\u7b97"));
                    IotSeatSession finishedSession = seatSessionOperationService.finishSession(activeSession.getId(), finishRequest);
                    chargeAmount = defaultRate(finishedSession.getChargeAmount());
                } else if (seat.getCurrentSessionStartedAt() != null) {
                    chargeAmount = calculateChargeAmount(seat.getCurrentSessionStartedAt(), seat.getHourlyRate());
                }
                seat.setPowerStatus("off");
                seat.setSeatStatus("idle");
                seat.setCurrentUserId(null);
                seat.setCurrentSessionStartedAt(null);
                iotSeatService.updateById(seat);
            }

            updatedSeats.add(seat);

            if (!changed) {
                continue;
            }

            String action = occupied ? "power_on" : "power_off";
            iotRelayActionService.recordAction(seat.getId(), seat.getRelayDeviceId(), seat.getRelayChannel(),
                    action, source, "success", remark);

            String message = occupied
                    ? resolveSeatLabel(seat) + "\u540c\u6b65\u5f00\u542f\uff0c\u4f7f\u7528\u8005\uff1a" + resolveUserName(currentUser)
                    : resolveSeatLabel(seat) + "\u540c\u6b65\u5173\u95ed\uff0c\u4f7f\u7528\u8005\uff1a" + resolveUserName(currentUser)
                        + "\uff0c\u6d88\u8d39\u91d1\u989d\uff1a" + formatAmount(chargeAmount) + "\u5143";

            iotSystemLogService.recordLog(
                    seat.getRelayDeviceId(),
                    seat.getId(),
                    currentUser == null ? null : currentUser.getId(),
                    "MQTT",
                    "info",
                    message,
                    "{\"source\":\"" + source + "\",\"deviceCode\":\"" + deviceCode
                            + "\",\"occupied\":" + occupied + ",\"chargeAmount\":\"" + formatAmount(chargeAmount) + "\"}");

            streamService.publish(new StreamEventVO(occupied ? "seat-powered-on" : "seat-powered-off", seat, AppTime.now()));
        }

        return updatedSeats;
    }

    private IotSeat requireSeat(Long seatId) {
        IotSeat seat = iotSeatService.getById(seatId);
        if (seat == null) {
            throw new IllegalArgumentException("Seat not found: " + seatId);
        }
        return seat;
    }

    private IotSeat requireSeat(SeatStateSyncItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Seat item is missing");
        }
        if (item.getSeatId() != null) {
            return requireSeat(item.getSeatId());
        }
        String normalizedSeatCode = normalizeSeatCode(item.getSeatCode());
        if (normalizedSeatCode.isBlank()) {
            throw new IllegalArgumentException("Seat code is missing");
        }

        IotSeat seat = iotSeatService.getOne(new LambdaQueryWrapper<IotSeat>()
                .eq(IotSeat::getSeatCode, normalizedSeatCode)
                .last("LIMIT 1"), false);
        if (seat != null) {
            return seat;
        }

        seat = iotSeatService.list().stream()
                .filter(candidate -> normalizedSeatCode.equals(normalizeSeatCode(candidate.getSeatCode()))
                        || normalizedSeatCode.equals(normalizeSeatCode(candidate.getSeatName())))
                .findFirst()
                .orElse(null);
        if (seat == null) {
            throw new IllegalArgumentException("Seat not found: " + item.getSeatCode());
        }
        return seat;
    }

    private String defaultText(String text, String fallback) {
        return text == null || text.isBlank() ? fallback : text;
    }

    private String normalizeSeatCode(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim()
                .toLowerCase(Locale.ROOT)
                .replace("seat", "")
                .replace("-", "")
                .replace("_", "")
                .replace(" ", "");
        if (normalized.matches("\\d+")) {
            return String.format(Locale.ROOT, "%02d", Integer.parseInt(normalized));
        }
        return normalized;
    }

    private String resolveSeatLabel(IotSeat seat) {
        if (seat.getSeatCode() != null && !seat.getSeatCode().isBlank()) {
            return seat.getSeatCode();
        }
        if (seat.getSeatName() != null && !seat.getSeatName().isBlank()) {
            return seat.getSeatName();
        }
        return "Seat-" + seat.getId();
    }

    private IotUser resolveTemporaryUser() {
        IotUser tempUser = iotUserService.getOne(new LambdaQueryWrapper<IotUser>()
                .eq(IotUser::getUserCode, TEMP_USER_CODE)
                .last("LIMIT 1"), false);
        if (tempUser != null) {
            if (!TEMP_USER_NAME.equals(tempUser.getFullName())) {
                tempUser.setFullName(TEMP_USER_NAME);
                tempUser.setIdentityStatus("visitor");
                iotUserService.updateById(tempUser);
            }
            return tempUser;
        }

        tempUser = new IotUser();
        tempUser.setUserCode(TEMP_USER_CODE);
        tempUser.setFullName(TEMP_USER_NAME);
        tempUser.setIdentityStatus("visitor");
        tempUser.setRemark("system-generated temporary user");
        iotUserService.save(tempUser);
        return tempUser;
    }

    private IotUser resolveSeatUser(IotSeat seat) {
        if (seat.getCurrentUserId() == null) {
            return null;
        }
        return iotUserService.getById(seat.getCurrentUserId());
    }

    private IotSeatSession ensureActiveSession(IotSeat seat, Long userId, String source, LocalDateTime startedAt) {
        IotSeatSession activeSession = iotSeatSessionService.getActiveSessionBySeatId(seat.getId());
        if (activeSession != null) {
            return activeSession;
        }

        IotSeatSession session = new IotSeatSession();
        session.setSeatId(seat.getId());
        session.setUserId(userId);
        session.setSessionSource(normalizeSessionSource(source));
        session.setStartedAt(startedAt == null ? AppTime.now() : startedAt);
        session.setDurationSeconds(0);
        session.setBillingHours(0);
        session.setHourlyRate(defaultRate(seat.getHourlyRate()));
        session.setChargeAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        session.setSessionStatus("active");
        iotSeatSessionService.save(session);
        return session;
    }

    private BigDecimal calculateChargeAmount(LocalDateTime startedAt, BigDecimal hourlyRate) {
        if (startedAt == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        int durationSeconds = Math.max((int) Duration.between(startedAt, AppTime.now()).getSeconds(), 0);
        int billingHours = Math.max(1, (int) Math.ceil(durationSeconds / 3600.0));
        return defaultRate(hourlyRate).multiply(BigDecimal.valueOf(billingHours)).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal defaultRate(BigDecimal rate) {
        return rate == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : rate.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeSessionSource(String source) {
        String value = defaultText(source, "manual").trim().toLowerCase(Locale.ROOT);
        if (value.contains("rfid") || value.contains("auth")) {
            return "rfid_auth";
        }
        if (value.contains("restore") || value.contains("system")) {
            return "system_restore";
        }
        return "manual";
    }

    private String resolveUserName(IotUser user) {
        return user == null || user.getFullName() == null || user.getFullName().isBlank() ? "\u65e0\u4eba" : user.getFullName();
    }

    private String formatAmount(BigDecimal amount) {
        return defaultRate(amount).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
