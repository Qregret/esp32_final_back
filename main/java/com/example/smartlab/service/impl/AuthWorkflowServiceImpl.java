package com.example.smartlab.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.smartlab.dto.AuthEventCreateRequest;
import com.example.smartlab.dto.SeatPowerRequest;
import com.example.smartlab.dto.StartSeatSessionRequest;
import com.example.smartlab.entity.IotAuthEvent;
import com.example.smartlab.entity.IotSeat;
import com.example.smartlab.entity.IotUser;
import com.example.smartlab.service.AuthWorkflowService;
import com.example.smartlab.service.IotAuthEventService;
import com.example.smartlab.service.IotSeatService;
import com.example.smartlab.service.IotSystemLogService;
import com.example.smartlab.service.IotUserService;
import com.example.smartlab.service.SeatOperationService;
import com.example.smartlab.service.SeatSessionOperationService;
import com.example.smartlab.support.AppTime;
import com.example.smartlab.service.StreamService;
import com.example.smartlab.vo.StreamEventVO;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthWorkflowServiceImpl implements AuthWorkflowService {

    private final IotAuthEventService iotAuthEventService;
    private final IotUserService iotUserService;
    private final IotSeatService iotSeatService;
    private final SeatOperationService seatOperationService;
    private final SeatSessionOperationService seatSessionOperationService;
    private final IotSystemLogService iotSystemLogService;
    private final StreamService streamService;

    public AuthWorkflowServiceImpl(IotAuthEventService iotAuthEventService,
                                   IotUserService iotUserService,
                                   IotSeatService iotSeatService,
                                   SeatOperationService seatOperationService,
                                   SeatSessionOperationService seatSessionOperationService,
                                   IotSystemLogService iotSystemLogService,
                                   StreamService streamService) {
        this.iotAuthEventService = iotAuthEventService;
        this.iotUserService = iotUserService;
        this.iotSeatService = iotSeatService;
        this.seatOperationService = seatOperationService;
        this.seatSessionOperationService = seatSessionOperationService;
        this.iotSystemLogService = iotSystemLogService;
        this.streamService = streamService;
    }

    @Override
    @Transactional
    public IotAuthEvent handle(AuthEventCreateRequest request) {
        IotAuthEvent event = request.getAuthEventId() == null
                ? new IotAuthEvent()
                : iotAuthEventService.getById(request.getAuthEventId());
        if (event == null) {
            throw new IllegalArgumentException("Auth event not found: " + request.getAuthEventId());
        }

        if (event.getCreatedAt() == null) {
            event.setCreatedAt(AppTime.now());
        }
        mergeRequest(event, request);
        applyPolicy(event);

        if (event.getId() == null) {
            iotAuthEventService.save(event);
        } else {
            iotAuthEventService.updateById(event);
        }

        iotSystemLogService.recordLog(event.getGatewayDeviceId(), event.getSeatId(), event.getUserId(),
                "AUTH", "info", event.getStatusText(),
                "{\"flowState\":\"" + event.getFlowState() + "\",\"authResult\":\"" + event.getAuthResult() + "\"}");

        if ("granted".equalsIgnoreCase(event.getAuthResult()) && event.getSeatId() != null && event.getUserId() != null) {
            SeatPowerRequest powerRequest = new SeatPowerRequest();
            powerRequest.setUserId(event.getUserId());
            powerRequest.setSource("auth_success");
            powerRequest.setRemark("Power on from successful auth");
            seatOperationService.powerOn(event.getSeatId(), powerRequest);

            IotSeat seat = iotSeatService.getById(event.getSeatId());
            if (seat.getCurrentUserId() == null) {
                StartSeatSessionRequest sessionRequest = new StartSeatSessionRequest();
                sessionRequest.setSeatId(event.getSeatId());
                sessionRequest.setUserId(event.getUserId());
                sessionRequest.setSessionSource(request.getSessionSource() == null ? "auth_event" : request.getSessionSource());
                seatSessionOperationService.startSession(sessionRequest);
            }
        }

        streamService.publish(new StreamEventVO("auth-event-updated", event, AppTime.now()));
        return event;
    }

    private void mergeRequest(IotAuthEvent event, AuthEventCreateRequest request) {
        event.setGatewayDeviceId(valueOrDefault(request.getGatewayDeviceId(), event.getGatewayDeviceId()));
        event.setCameraDeviceId(valueOrDefault(request.getCameraDeviceId(), event.getCameraDeviceId()));
        event.setRfidDeviceId(valueOrDefault(request.getRfidDeviceId(), event.getRfidDeviceId()));
        event.setUserId(valueOrDefault(request.getUserId(), event.getUserId()));
        event.setSeatId(valueOrDefault(request.getSeatId(), event.getSeatId()));
        event.setRfidUid(textOrDefault(request.getRfidUid(), event.getRfidUid()));
        event.setCameraStatus(textOrDefault(request.getCameraStatus(), event.getCameraStatus()));
        event.setFlowState(textOrDefault(request.getFlowState(), event.getFlowState()));
        event.setSimilarity(numberOrDefault(request.getSimilarity(), event.getSimilarity()));
        event.setAuthResult(textOrDefault(request.getAuthResult(), event.getAuthResult()));
        event.setStatusText(textOrDefault(request.getStatusText(), event.getStatusText()));
        event.setSnapshotUrl(textOrDefault(request.getSnapshotUrl(), event.getSnapshotUrl()));
    }

    private void applyPolicy(IotAuthEvent event) {
        if (event.getFlowState() == null || event.getFlowState().isBlank()) {
            event.setFlowState("processing");
        }
        if ("idle".equalsIgnoreCase(event.getFlowState())) {
            event.setAuthResult(defaultBlank(event.getAuthResult(), "standby"));
            event.setStatusText(defaultBlank(event.getStatusText(), "Waiting for next auth request."));
            return;
        }

        if (event.getRfidUid() == null || event.getRfidUid().isBlank()) {
            deny(event, "error", "denied", "RFID not detected.");
            return;
        }

        IotUser user = resolveUser(event);
        if (user == null) {
            deny(event, "error", "denied", "RFID is not bound to any user.");
            return;
        }
        event.setUserId(user.getId());

        if (!"active".equalsIgnoreCase(user.getIdentityStatus())) {
            deny(event, "error", "denied", "User is not active.");
            return;
        }

        if (event.getSeatId() == null) {
            deny(event, "error", "denied", "Seat is required.");
            return;
        }

        IotSeat seat = iotSeatService.getById(event.getSeatId());
        if (seat == null) {
            deny(event, "error", "denied", "Seat not found.");
            return;
        }
        if ("occupied".equalsIgnoreCase(seat.getSeatStatus()) && !user.getId().equals(seat.getCurrentUserId())) {
            deny(event, "error", "denied", "Seat is already occupied.");
            return;
        }

        BigDecimal similarity = event.getSimilarity() == null ? BigDecimal.ZERO : event.getSimilarity();
        if (similarity.compareTo(BigDecimal.valueOf(80)) < 0) {
            deny(event, "error", "denied", "Face similarity is below threshold.");
            return;
        }

        event.setFlowState(defaultBlank(event.getFlowState(), "success"));
        event.setAuthResult(defaultBlank(event.getAuthResult(), "granted"));
        event.setStatusText(defaultBlank(event.getStatusText(), "Auth granted. Starting seat linkage."));
    }

    private IotUser resolveUser(IotAuthEvent event) {
        if (event.getUserId() != null) {
            return iotUserService.getById(event.getUserId());
        }
        return iotUserService.getOne(new LambdaQueryWrapper<IotUser>()
                .eq(IotUser::getRfidUid, event.getRfidUid())
                .last("LIMIT 1"));
    }

    private void deny(IotAuthEvent event, String flowState, String authResult, String statusText) {
        event.setFlowState(flowState);
        event.setAuthResult(authResult);
        event.setStatusText(statusText);
    }

    private Long valueOrDefault(Long newValue, Long oldValue) {
        return newValue != null ? newValue : oldValue;
    }

    private String textOrDefault(String newValue, String oldValue) {
        return newValue != null ? newValue : oldValue;
    }

    private BigDecimal numberOrDefault(BigDecimal newValue, BigDecimal oldValue) {
        return newValue != null ? newValue : oldValue;
    }

    private String defaultBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
