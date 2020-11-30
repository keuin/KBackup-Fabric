package com.keuin.kbackupfabric.util.backup.formatter;

import com.keuin.kbackupfabric.util.backup.BackupFilesystemUtil;
import com.keuin.kbackupfabric.util.backup.BackupNameTimeFormatter;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrimitiveZipBackupFileNameFormatter implements BackupFileNameFormatter {

    private static final PrimitiveZipBackupFileNameFormatter instance = new PrimitiveZipBackupFileNameFormatter();

    public static PrimitiveZipBackupFileNameFormatter getInstance() {
        return instance;
    }

    @Override
    public BackupFileNameFormatter.BackupFileName format(String fileName) {
        LocalDateTime time = getTime(fileName);
        String name = getBackupName(fileName);
        return new BackupFileNameFormatter.BackupFileName(time,name);
    }

    private LocalDateTime getTime(String fileName) {
        Matcher matcher = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}_[0-9]{2}-[0-9]{2}-[0-9]{2}").matcher(fileName);
        if (matcher.find()) {
            String timeString = matcher.group(0);
            return BackupNameTimeFormatter.timeStringToLocalDateTime(timeString);
        }
        return null;
    }

    private String getBackupName(String backupFileName) {
        try {
            if (backupFileName.matches(BackupFilesystemUtil.getBackupFileNamePrefix() + ".+\\.zip"))
                return backupFileName.substring(BackupFilesystemUtil.getBackupFileNamePrefix().length(), backupFileName.length() - 4);
        } catch (IndexOutOfBoundsException ignored) {
        }
        return backupFileName;
    }

}
