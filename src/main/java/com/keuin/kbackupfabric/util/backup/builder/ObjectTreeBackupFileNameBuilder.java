package com.keuin.kbackupfabric.util.backup.builder;

import com.keuin.kbackupfabric.util.backup.BackupFilesystemUtil;
import com.keuin.kbackupfabric.util.backup.BackupNameTimeFormatter;

import java.time.LocalDateTime;

public class ObjectTreeBackupFileNameBuilder implements BackupFileNameBuilder {
    private static final ObjectTreeBackupFileNameBuilder instance = new ObjectTreeBackupFileNameBuilder();

    public static ObjectTreeBackupFileNameBuilder getInstance()  {
        return instance;
    }

    @Override
    public String build(LocalDateTime time, String backupName) {
        String timeString = BackupNameTimeFormatter.localDateTimeToString(time);
        return String.format("%s%s_%s%s", BackupFilesystemUtil.getBackupFileNamePrefix(),timeString,backupName,".json");
    }
}
