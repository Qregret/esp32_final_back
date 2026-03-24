package com.example.smartlab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("iot_relay_actions")
public class IotRelayAction {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long seatId;
    private Long relayDeviceId;
    private Integer relayChannel;
    private String actionType;
    private String actionSource;
    private String action;
    private String triggerSource;
    private String operatorName;
    private String actionResult;
    private String actionMessage;
    private LocalDateTime executedAt;
    private LocalDateTime createdAt;
}
