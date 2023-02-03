package com.jkddg.nvrmailclient.util;

import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * @Author 黄永好
 * @create 2023/2/3 15:18
 */
public class DateUtil {
    private static final String PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String FOR_MAT_PATTEN_2 = "yyyyMM";
    private static final String FOR_MAT_PATTEN_3 = "yyyyMMdd";

    private static final String TIME_PATTEN = "HH:mm:ss";

    /**
     * LocalDateTime 转str
     */
    public static String localDateTime2Str(LocalDateTime localDateTime, String pattern) {
        return localDateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String localDateTime2Str(LocalDateTime localDateTime) {
        return localDateTime2Str(localDateTime, PATTERN);
    }

    public static LocalDateTime getDateTimeFromStr(String datetime) {
        if (StringUtils.isEmpty(datetime)) {
            return null;
        }
        DateTimeFormatter df = DateTimeFormatter.ofPattern(PATTERN);
        return LocalDateTime.parse(datetime, df);
    }

    public static LocalTime getTimeFromStr(String time) {
        if (StringUtils.isEmpty(time)) {
            return null;
        }
        DateTimeFormatter df = DateTimeFormatter.ofPattern(TIME_PATTEN);
        return LocalTime.parse(time, df);
    }

    /**
     * LocalDateTime 转str
     */
    public static String localDateTime2YM(LocalDateTime localDateTime) {
        return localDateTime.toLocalDate().format(DateTimeFormatter.ofPattern(FOR_MAT_PATTEN_2));
    }

    /**
     * 格式化 00:00:00
     */
    public static LocalDateTime parseLocalDateTimeMin(LocalDateTime localDateTime) {
        return LocalDateTime.of(localDateTime.toLocalDate(), LocalTime.of(0, 0, 0));
    }

    /**
     * 格式化 23:59:59
     */
    public static LocalDateTime parseLocalDateTimeMax(LocalDateTime localDateTime) {
        return LocalDateTime.of(localDateTime.toLocalDate(), LocalTime.of(23, 59, 59));
    }

    public static long dateTimeToTimestamp(LocalDateTime ldt) {
        return ldt.toInstant(ZoneOffset.of("+8")).toEpochMilli();
    }

    /**
     * 10 位时间戳
     *
     * @return 10 位时间戳
     */
    public static long dateTimeToTimestamp() {
        return LocalDateTime.now().toInstant(ZoneOffset.ofHours(8)).getEpochSecond();
    }

    /**
     * yyyyMMdd
     *
     * @param localDate 时间
     * @return
     */
    public static String localDate2Str(LocalDate localDate) {
        return localDate.format(DateTimeFormatter.ofPattern(FOR_MAT_PATTEN_3));
    }

    public static boolean chkInBetweenNow(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            return false;
        }
        LocalTime nowTime = LocalTime.now();
        return nowTime.isAfter(startTime) && nowTime.isBefore(endTime);
    }
}
