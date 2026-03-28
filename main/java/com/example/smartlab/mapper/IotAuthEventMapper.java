package com.example.smartlab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.smartlab.entity.IotAuthEvent;
import com.example.smartlab.vo.AuthEventDetailVO;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface IotAuthEventMapper extends BaseMapper<IotAuthEvent> {

    @Select("""
            <script>
            SELECT
                ae.id AS authEventId,
                ae.flow_state AS flowState,
                ae.auth_result AS authResult,
                ae.similarity AS similarity,
                ae.camera_status AS cameraStatus,
                ae.status_text AS statusText,
                ae.rfid_uid AS rfidUid,
                ae.snapshot_url AS snapshotUrl,
                ae.created_at AS createdAt,
                u.id AS userId,
                u.user_code AS userCode,
                u.full_name AS userName,
                u.identity_status AS identityStatus,
                s.id AS seatId,
                s.seat_code AS seatCode,
                s.seat_name AS seatName,
                s.seat_status AS seatStatus,
                gw.id AS gatewayDeviceId,
                gw.device_code AS gatewayDeviceCode,
                gw.device_name AS gatewayDeviceName,
                cam.id AS cameraDeviceId,
                cam.device_code AS cameraDeviceCode,
                cam.device_name AS cameraDeviceName,
                rf.id AS rfidDeviceId,
                rf.device_code AS rfidDeviceCode,
                rf.device_name AS rfidDeviceName
            FROM iot_auth_events ae
            LEFT JOIN iot_users u ON ae.user_id = u.id
            LEFT JOIN iot_seats s ON ae.seat_id = s.id
            LEFT JOIN iot_devices gw ON ae.gateway_device_id = gw.id
            LEFT JOIN iot_devices cam ON ae.camera_device_id = cam.id
            LEFT JOIN iot_devices rf ON ae.rfid_device_id = rf.id
            <where>
                <if test="authResult != null and authResult != ''">
                    ae.auth_result = #{authResult}
                </if>
            </where>
            ORDER BY ae.created_at DESC
            </script>
            """)
    List<AuthEventDetailVO> selectAuthEventDetails(@Param("authResult") String authResult);
}
