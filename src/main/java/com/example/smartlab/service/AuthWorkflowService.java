package com.example.smartlab.service;

import com.example.smartlab.dto.AuthEventCreateRequest;
import com.example.smartlab.entity.IotAuthEvent;

public interface AuthWorkflowService {

    IotAuthEvent handle(AuthEventCreateRequest request);
}
