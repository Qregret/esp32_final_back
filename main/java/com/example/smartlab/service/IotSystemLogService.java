package com.example.smartlab.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.smartlab.entity.IotSystemLog;
import java.util.List;

public interface IotSystemLogService extends IService<IotSystemLog> {

    List<IotSystemLog> listLatest(int limit);

    IotSystemLog recordLog(Long deviceId, Long seatId, Long userId, String logType, String logLevel,
                           String message, String rawPayload);
}
