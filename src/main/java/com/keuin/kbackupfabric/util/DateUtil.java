package com.keuin.kbackupfabric.util;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtil {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final SimpleDateFormat outFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter prettyFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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

    public static String fromEpochMillis(long epochMillis) {
        return outFormatter.format(new Date(epochMillis));
    }

    public static String getPrettyString(LocalDateTime localDateTime) {
        return prettyFormatter.format(localDateTime);
    }
}
