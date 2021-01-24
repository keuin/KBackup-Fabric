package com.keuin.kbackupfabric.backup;

import com.keuin.kbackupfabric.util.DateUtil;

import java.time.LocalDateTime;

public class BackupNameTimeFormatter {

    @Deprecated
    public static String getTimeString() {
        return DateUtil.getString(LocalDateTime.now());
    }

    public static String localDateTimeToString(LocalDateTime localDateTime) {
        return DateUtil.getString(localDateTime);
    }

    @Deprecated
    public static long timeStringToEpochSeconds(String timeString) {
        return DateUtil.toEpochSecond(timeString);
    }

}
