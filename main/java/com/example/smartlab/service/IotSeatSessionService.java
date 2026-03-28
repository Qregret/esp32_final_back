package com.example.smartlab.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.smartlab.entity.IotSeatSession;
import java.util.List;

public interface IotSeatSessionService extends IService<IotSeatSession> {

    List<IotSeatSession> listActiveSessions();

    IotSeatSession getActiveSessionBySeatId(Long seatId);
}
