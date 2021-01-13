package com.keuin.kbackupfabric.util.backup.name;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IncrementalBackupFileNameEncoder implements BackupFileNameEncoder {
    private static final String backupFileNamePrefix = "incremental";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    // TODO: make this private and use singleton pattern
    public IncrementalBackupFileNameEncoder() {
    }

    @Override
    public String encode(String customName, LocalDateTime time) {
        if (!isValidCustomName(customName))
            throw new IllegalArgumentException("Invalid custom name");
        String timeString = time.format(formatter);
        return backupFileNamePrefix + "-" + timeString + "_" + customName + ".kbi";
    }

    @Override
    public BackupFileNameEncoder.BackupBasicInformation decode(String fileName) {
        Pattern pattern = Pattern.compile(
                backupFileNamePrefix + "-" + "([0-9]{4}-[0-9]{2}-[0-9]{2}_[0-9]{2}-[0-9]{2}-[0-9]{2})_(.+)\\.kbi"
        );
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.find()) {
            String timeString = matcher.group(1);
            String customName = matcher.group(2);
            return new BackupFileNameEncoder.BackupBasicInformation(customName, LocalDateTime.parse(timeString, formatter));
        }
        return null;
    }
}
