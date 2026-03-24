package com.example.smartlab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.smartlab.entity.IotSeat;
import com.example.smartlab.vo.SeatOverviewVO;
import java.util.List;
import org.apache.ibatis.annotations.Select;

public interface IotSeatMapper extends BaseMapper<IotSeat> {

    @Select("""
            SELECT
                s.id AS seatId,
                s.seat_code AS seatCode,
                s.seat_name AS seatName,
                s.seat_status AS seatStatus,
                s.power_status AS powerStatus,
                s.relay_channel AS relayChannel,
                s.hourly_rate AS hourlyRate,
                s.current_session_started_at AS currentSessionStartedAt,
                u.id AS currentUserId,
                u.user_code AS currentUserCode,
                u.full_name AS currentUserName,
                u.phone_no AS currentUserPhone,
                d.id AS relayDeviceId,
                d.device_code AS relayDeviceCode,
                d.device_name AS relayDeviceName,
                d.online_status AS relayDeviceStatus
            FROM iot_seats s
            LEFT JOIN iot_users u ON s.current_user_id = u.id
            LEFT JOIN iot_devices d ON s.relay_device_id = d.id
            ORDER BY s.id
            """)
    List<SeatOverviewVO> selectSeatOverview();
}
