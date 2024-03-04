package com.example.projecmntserver.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constant {
    public static final String DATE_PATTERN = "yyyyMMdd";
    public static final String MONTH_PATTERN = "yyyy-MM";
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static final Integer TIME_MM = 20 * 8 * 3600;
    public static final Integer TIME_MD = 8 * 3600;
}
