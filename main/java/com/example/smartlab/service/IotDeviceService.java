package com.example.smartlab.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.smartlab.entity.IotDevice;
import java.util.List;

public interface IotDeviceService extends IService<IotDevice> {

    List<IotDevice> listByDeviceType(String deviceType);

    List<IotDevice> listOnlineDevices();
}
