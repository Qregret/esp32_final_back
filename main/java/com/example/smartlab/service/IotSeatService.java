package com.example.smartlab.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.smartlab.entity.IotSeat;
import com.example.smartlab.vo.SeatOverviewVO;
import java.util.List;

public interface IotSeatService extends IService<IotSeat> {

    List<IotSeat> listOccupiedSeats();

    List<IotSeat> listByCurrentUserId(Long userId);

    List<SeatOverviewVO> listSeatOverview();
}
