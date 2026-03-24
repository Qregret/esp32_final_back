package com.example.smartlab.controller;

import com.example.smartlab.entity.IotUser;
import com.example.smartlab.service.IotUserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class IotUserController {

    private final IotUserService iotUserService;

    @GetMapping
    public List<IotUser> listAll() {
        return iotUserService.list();
    }

    @GetMapping("/active")
    public List<IotUser> listActive() {
        return iotUserService.listActiveUsers();
    }
}
