package com.example.smartlab.support;

import java.time.LocalDateTime;
import java.time.ZoneId;

public final class AppTime {

    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Shanghai");

    private AppTime() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(APP_ZONE);
    }
}
