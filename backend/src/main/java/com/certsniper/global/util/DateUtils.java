package com.certsniper.global.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** 서비스 기준 시간대(Asia/Seoul) */
public final class DateUtils {

    public static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private DateUtils() {
    }

    public static LocalDate today() {
        return LocalDate.now(ZONE);
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(ZONE);
    }
}
