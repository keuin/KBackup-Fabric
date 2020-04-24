package com.keuin.kbackupfabric.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class BackupNameTimeFormatter {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    public static String getTimeString() {
        return LocalDateTime.now().format(formatter);
    }

    public static long timeStringToEpochSeconds(String timeString) {
        ZoneId systemZone = ZoneId.systemDefault(); // my timezone
        LocalDateTime localDateTime = LocalDateTime.parse(timeString, formatter);
        ZoneOffset currentOffsetForMyZone = systemZone.getRules().getOffset(localDateTime);
        return localDateTime.toEpochSecond(currentOffsetForMyZone);
    }

}
