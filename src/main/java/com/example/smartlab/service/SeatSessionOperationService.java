package com.example.smartlab.service;

import com.example.smartlab.dto.FinishSeatSessionRequest;
import com.example.smartlab.dto.StartSeatSessionRequest;
import com.example.smartlab.entity.IotSeatSession;

public interface SeatSessionOperationService {

    IotSeatSession startSession(StartSeatSessionRequest request);

    IotSeatSession finishSession(Long sessionId, FinishSeatSessionRequest request);
}
