package com.example.smartlab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("iot_system_logs")
public class IotSystemLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long deviceId;
    private Long seatId;
    private Long userId;
    private String logType;
    private String logLevel;
    private String message;
    private String rawPayload;
    private LocalDateTime createdAt;
}
