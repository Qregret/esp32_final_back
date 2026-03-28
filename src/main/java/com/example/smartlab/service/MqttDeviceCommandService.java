package com.example.smartlab.service;

import com.example.smartlab.entity.IotSeat;

public interface MqttDeviceCommandService {

    void publishSeatPowerCommand(IotSeat seat, boolean powerOn, String source, String remark);
}
