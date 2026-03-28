package com.example.smartlab.service;

import com.example.smartlab.dto.EnvironmentReadingCreateRequest;
import com.example.smartlab.entity.IotEnvironmentReading;

public interface EnvironmentOperationService {

    IotEnvironmentReading createReading(EnvironmentReadingCreateRequest request);
}
