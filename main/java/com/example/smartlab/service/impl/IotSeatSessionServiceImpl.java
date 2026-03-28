package com.example.smartlab.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.smartlab.entity.IotSeatSession;
import com.example.smartlab.mapper.IotSeatSessionMapper;
import com.example.smartlab.service.IotSeatSessionService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class IotSeatSessionServiceImpl extends ServiceImpl<IotSeatSessionMapper, IotSeatSession> implements IotSeatSessionService {

    @Override
    public List<IotSeatSession> listActiveSessions() {
        return list(new LambdaQueryWrapper<IotSeatSession>()
                .eq(IotSeatSession::getSessionStatus, "active")
                .orderByDesc(IotSeatSession::getStartedAt));
    }

    @Override
    public IotSeatSession getActiveSessionBySeatId(Long seatId) {
        return getOne(new LambdaQueryWrapper<IotSeatSession>()
                .eq(IotSeatSession::getSeatId, seatId)
                .eq(IotSeatSession::getSessionStatus, "active")
                .orderByDesc(IotSeatSession::getStartedAt)
                .last("LIMIT 1"));
    }
}
