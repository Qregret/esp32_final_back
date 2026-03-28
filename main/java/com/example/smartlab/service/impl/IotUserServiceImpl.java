package com.example.smartlab.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.smartlab.entity.IotUser;
import com.example.smartlab.mapper.IotUserMapper;
import com.example.smartlab.service.IotUserService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class IotUserServiceImpl extends ServiceImpl<IotUserMapper, IotUser> implements IotUserService {

    @Override
    public List<IotUser> listActiveUsers() {
        return list(new LambdaQueryWrapper<IotUser>()
                .eq(IotUser::getIdentityStatus, "active")
                .orderByAsc(IotUser::getId));
    }
}
