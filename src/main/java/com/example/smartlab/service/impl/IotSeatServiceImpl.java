package com.example.smartlab.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.smartlab.entity.IotSeat;
import com.example.smartlab.mapper.IotSeatMapper;
import com.example.smartlab.service.IotSeatService;
import com.example.smartlab.vo.SeatOverviewVO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class IotSeatServiceImpl extends ServiceImpl<IotSeatMapper, IotSeat> implements IotSeatService {

    @Override
    public List<IotSeat> listOccupiedSeats() {
        return list(new LambdaQueryWrapper<IotSeat>()
                .eq(IotSeat::getSeatStatus, "occupied")
                .orderByAsc(IotSeat::getId));
    }

    @Override
    public List<IotSeat> listByCurrentUserId(Long userId) {
        return list(new LambdaQueryWrapper<IotSeat>()
                .eq(IotSeat::getCurrentUserId, userId)
                .orderByAsc(IotSeat::getId));
    }

    @Override
    public List<SeatOverviewVO> listSeatOverview() {
        return baseMapper.selectSeatOverview();
    }
}
