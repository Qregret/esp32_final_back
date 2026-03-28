package com.example.smartlab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("iot_users")
public class IotUser {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String userCode;
    private String fullName;
    private String rfidUid;
    private String faceTemplateId;
    private String phoneNo;
    private String identityStatus;
    private String remark;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
}
