package com.example.projecmntserver.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import com.example.projecmntserver.constant.CommonConstant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatetimeUtils {
    public static LocalDate parse(String dateStr) {
        return LocalDate.parse(dateStr,
                               DateTimeFormatter.ofPattern(
                                       CommonConstant.DATE_PATTERN_1));
    }

    public static String toDate(LocalDate date, @Nullable String pattern) {
        return date.format(DateTimeFormatter.ofPattern(
                StringUtils.hasText(pattern) ? pattern : CommonConstant.DATE_PATTERN_2));
    }
}
