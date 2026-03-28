package com.example.smartlab.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.smartlab.entity.IotAuthEvent;
import com.example.smartlab.mapper.IotAuthEventMapper;
import com.example.smartlab.service.IotAuthEventService;
import com.example.smartlab.vo.AuthEventDetailVO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class IotAuthEventServiceImpl extends ServiceImpl<IotAuthEventMapper, IotAuthEvent> implements IotAuthEventService {

    @Override
    public List<IotAuthEvent> listLatest(int limit) {
        return list(new LambdaQueryWrapper<IotAuthEvent>()
                .orderByDesc(IotAuthEvent::getCreatedAt)
                .last("LIMIT " + Math.max(limit, 1)));
    }

    @Override
    public List<IotAuthEvent> listGrantedEvents() {
        return list(new LambdaQueryWrapper<IotAuthEvent>()
                .eq(IotAuthEvent::getAuthResult, "granted")
                .orderByDesc(IotAuthEvent::getCreatedAt));
    }

    @Override
    public List<AuthEventDetailVO> listAuthEventDetails(String authResult) {
        return baseMapper.selectAuthEventDetails(authResult);
    }

    @Override
    public AuthEventDetailVO getLatestDetail() {
        List<AuthEventDetailVO> details = baseMapper.selectAuthEventDetails(null);
        return details.isEmpty() ? null : details.get(0);
    }
}
