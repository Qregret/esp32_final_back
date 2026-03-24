package com.example.smartlab.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.smartlab.entity.IotEnvironmentReading;

public interface IotEnvironmentReadingService extends IService<IotEnvironmentReading> {

    IotEnvironmentReading getLatestReading();
}
