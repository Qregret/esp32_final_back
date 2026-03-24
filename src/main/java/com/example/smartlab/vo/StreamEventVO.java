package com.example.smartlab.vo;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StreamEventVO {

    private String eventType;
    private Object payload;
    private LocalDateTime timestamp;
}
