package com.example.smartlab.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.smartlab.entity.IotDevice;
import com.example.smartlab.mapper.IotDeviceMapper;
import com.example.smartlab.service.IotDeviceService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class IotDeviceServiceImpl extends ServiceImpl<IotDeviceMapper, IotDevice> implements IotDeviceService {

    @Override
    public List<IotDevice> listByDeviceType(String deviceType) {
        return list(new LambdaQueryWrapper<IotDevice>()
                .eq(IotDevice::getDeviceType, deviceType)
                .orderByAsc(IotDevice::getId));
    }

    @Override
    public List<IotDevice> listOnlineDevices() {
        return list(new LambdaQueryWrapper<IotDevice>()
                .eq(IotDevice::getOnlineStatus, "online")
                .orderByAsc(IotDevice::getId));
    }
}
