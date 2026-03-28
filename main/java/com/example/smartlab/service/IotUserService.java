package com.example.smartlab.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.smartlab.entity.IotUser;
import java.util.List;

public interface IotUserService extends IService<IotUser> {

    List<IotUser> listActiveUsers();
}
