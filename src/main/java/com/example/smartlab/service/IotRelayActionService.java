package com.example.smartlab.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.smartlab.entity.IotRelayAction;

public interface IotRelayActionService extends IService<IotRelayAction> {

    IotRelayAction recordAction(Long seatId, Long relayDeviceId, Integer relayChannel, String actionType,
                                String actionSource, String actionResult, String remark);
}
