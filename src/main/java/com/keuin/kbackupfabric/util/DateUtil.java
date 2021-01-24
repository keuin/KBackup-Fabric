package com.keuin.kbackupfabric.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    public static String getString(LocalDateTime localDateTime) {
        return localDateTime.format(formatter);
    }

    public static String getString(ZonedDateTime zonedDateTime) {
        return zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime().format(formatter);
    }

    @Deprecated
    public static long toEpochSecond(String dateTimeString) {
        ZoneId systemZone = ZoneId.systemDefault(); // my timezone
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeString, formatter);
        ZoneOffset currentOffsetForMyZone = systemZone.getRules().getOffset(localDateTime);
        return localDateTime.toEpochSecond(currentOffsetForMyZone);
    }

    public static LocalDateTime toLocalDateTime(String timeString) {
        return LocalDateTime.parse(timeString, formatter);
    }
}
