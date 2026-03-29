package com.example.smartlab.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.smartlab.entity.IotSystemLog;
import com.example.smartlab.mapper.IotSystemLogMapper;
import com.example.smartlab.service.IotSystemLogService;
import com.example.smartlab.service.StreamService;
import com.example.smartlab.support.AppTime;
import com.example.smartlab.vo.StreamEventVO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class IotSystemLogServiceImpl extends ServiceImpl<IotSystemLogMapper, IotSystemLog> implements IotSystemLogService {

    private final StreamService streamService;

    public IotSystemLogServiceImpl(StreamService streamService) {
        this.streamService = streamService;
    }

    @Override
    public List<IotSystemLog> listLatest(int limit) {
        return list(new LambdaQueryWrapper<IotSystemLog>()
                .orderByDesc(IotSystemLog::getCreatedAt)
                .last("LIMIT " + Math.max(limit, 1)));
    }

    @Override
    public IotSystemLog recordLog(Long deviceId, Long seatId, Long userId, String logType, String logLevel,
                                  String message, String rawPayload) {
        IotSystemLog log = new IotSystemLog();
        log.setDeviceId(deviceId);
        log.setSeatId(seatId);
        log.setUserId(userId);
        log.setLogType(logType);
        log.setLogLevel(logLevel);
        log.setMessage(message);
        log.setRawPayload(rawPayload);
        log.setCreatedAt(AppTime.now());
        save(log);
        streamService.publish(new StreamEventVO("system-log-created", log, AppTime.now()));
        return log;
    }
}
