package com.keuin.kbackupfabric.backup.name;

import com.keuin.kbackupfabric.util.DateUtil;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrimitiveBackupFileNameEncoder implements BackupFileNameEncoder {
    private static final String backupFileNamePrefix = "kbackup";

    @Override
    public String encode(String customName, LocalDateTime time) {
        if (!isValidCustomName(customName))
            throw new IllegalArgumentException("Invalid custom name");
        String timeString = DateUtil.getString(time);
        return backupFileNamePrefix + "-" + timeString + "_" + customName + ".zip";
    }

    @Override
    public BackupBasicInformation decode(String fileName) {
        Pattern pattern = Pattern.compile(
                "^" + backupFileNamePrefix + "-" + "([0-9]{4}-[0-9]{2}-[0-9]{2}_[0-9]{2}-[0-9]{2}-[0-9]{2})_(.+)\\.zip" + "$"
        );
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.find()) {
            String timeString = matcher.group(1);
            String customName = matcher.group(2);
            return new BackupBasicInformation(customName, DateUtil.toLocalDateTime(timeString));
        }
        return null;
    }
}
