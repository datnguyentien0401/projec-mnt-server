package com.example.projecmntserver.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import javax.validation.constraints.NotNull;

import org.springframework.util.StringUtils;

import com.example.projecmntserver.constant.Constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatetimeUtils {
    public static LocalDate parse(@NotNull String dateStr) {
        return LocalDate.parse(dateStr,
                               DateTimeFormatter.ofPattern(
                                       Constant.DATE_PATTERN));
    }

    public static String toMonth(@NotNull LocalDate date) {
        return toDate(date, Constant.MONTH_PATTERN);
    }

    public static String toDate(@NotNull LocalDate date, @NotNull String pattern) {
        return date.format(DateTimeFormatter.ofPattern(
                StringUtils.hasText(pattern) ? pattern : Constant.DATE_PATTERN));
    }

    public static LocalDateTime getCurrentLocalDateTime() {
        return LocalDateTime.now();
    }

    public static long countMonth(LocalDate fromDate, LocalDate toDate) {
        return ChronoUnit.MONTHS.between(fromDate, toDate) + 1;
    }
}
