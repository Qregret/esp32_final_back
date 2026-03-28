package com.example.smartlab.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.smartlab.entity.IotEnvironmentReading;
import com.example.smartlab.mapper.IotEnvironmentReadingMapper;
import com.example.smartlab.service.IotEnvironmentReadingService;
import org.springframework.stereotype.Service;

@Service
public class IotEnvironmentReadingServiceImpl extends ServiceImpl<IotEnvironmentReadingMapper, IotEnvironmentReading>
        implements IotEnvironmentReadingService {

    @Override
    public IotEnvironmentReading getLatestReading() {
        return getOne(new LambdaQueryWrapper<IotEnvironmentReading>()
                .orderByDesc(IotEnvironmentReading::getRecordedAt)
                .last("LIMIT 1"));
    }
}
