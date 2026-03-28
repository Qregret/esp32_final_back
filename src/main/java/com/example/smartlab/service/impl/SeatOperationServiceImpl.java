package com.example.smartlab.service.impl;

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
import com.example.smartlab.service.StreamService;
import com.example.smartlab.vo.StreamEventVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeatOperationServiceImpl implements SeatOperationService {

    private static final String TEMP_USER_CODE = "V0001";
    private static final String TEMP_USER_NAME = "临时使用者";

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
        seat.setPowerStatus("on");
        seat.setSeatStatus("occupied");
        if (seat.getCurrentUserId() == null) {
            seat.setCurrentUserId(resolveTemporaryUser().getId());
        }
        if (seat.getCurrentSessionStartedAt() == null) {
            seat.setCurrentSessionStartedAt(LocalDateTime.now());
        }
        iotSeatService.updateById(seat);

        String source = request == null ? "manual_control" : defaultText(request.getSource(), "manual_control");
        String remark = request == null ? "" : defaultText(request.getRemark(), "");
        iotRelayActionService.recordAction(seat.getId(), seat.getRelayDeviceId(), seat.getRelayChannel(),
                "power_on", source, "success", remark);

        String logMessage = resolveSeatLabel(seat) + " power on";
        iotSystemLogService.recordLog(seat.getRelayDeviceId(), seat.getId(), request == null ? null : request.getUserId(),
                "GPIO", "info", logMessage,
                "{\"action\":\"power_on\",\"source\":\"" + source + "\"}");

        streamService.publish(new StreamEventVO("seat-powered-on", seat, LocalDateTime.now()));
        mqttDeviceCommandService.publishSeatPowerCommand(seat, true, source, remark);
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
        seat.setSeatStatus("idle");
        seat.setCurrentUserId(null);
        seat.setCurrentSessionStartedAt(null);
        iotSeatService.updateById(seat);

        String source = request == null ? "manual_control" : defaultText(request.getSource(), "manual_control");
        String remark = request == null ? "" : defaultText(request.getRemark(), "");
        iotRelayActionService.recordAction(seat.getId(), seat.getRelayDeviceId(), seat.getRelayChannel(),
                "power_off", source, "success", remark);

        String logMessage = resolveSeatLabel(seat) + " power off";
        iotSystemLogService.recordLog(seat.getRelayDeviceId(), seat.getId(), request == null ? null : request.getUserId(),
                "GPIO", "info", logMessage,
                "{\"action\":\"power_off\",\"source\":\"" + source + "\"}");

        streamService.publish(new StreamEventVO("seat-powered-off", seat, LocalDateTime.now()));
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

            seat.setPowerStatus(targetPowerStatus);
            seat.setSeatStatus(targetSeatStatus);
            if (occupied) {
                if (seat.getCurrentUserId() == null) {
                    seat.setCurrentUserId(resolveTemporaryUser().getId());
                }
                if (seat.getCurrentSessionStartedAt() == null) {
                    seat.setCurrentSessionStartedAt(LocalDateTime.now());
                }
            } else {
                seat.setCurrentUserId(null);
                seat.setCurrentSessionStartedAt(null);
            }
            iotSeatService.updateById(seat);
            updatedSeats.add(seat);

            if (!changed) {
                continue;
            }

            String action = occupied ? "power_on" : "power_off";
            iotRelayActionService.recordAction(seat.getId(), seat.getRelayDeviceId(), seat.getRelayChannel(),
                    action, source, "success", remark);

            iotSystemLogService.recordLog(seat.getRelayDeviceId(), seat.getId(), null, "MQTT", "info",
                    resolveSeatLabel(seat) + " synced to " + targetSeatStatus,
                    "{\"source\":\"" + source + "\",\"deviceCode\":\"" + deviceCode
                            + "\",\"occupied\":" + occupied + "}");

            streamService.publish(new StreamEventVO(occupied ? "seat-powered-on" : "seat-powered-off", seat, LocalDateTime.now()));
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
        return "seat-" + seat.getId();
    }

    private IotUser resolveTemporaryUser() {
        IotUser tempUser = iotUserService.getOne(new LambdaQueryWrapper<IotUser>()
                .eq(IotUser::getUserCode, TEMP_USER_CODE)
                .last("LIMIT 1"), false);
        if (tempUser != null) {
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
}
