package com.keuin.kbackupfabric.util.backup.builder;


import java.time.LocalDateTime;

public interface BackupFileNameBuilder {

    static BackupFileNameBuilder primitiveZipBackup() {
        return PrimitiveZipBackupFileNameBuilder.getInstance();
    }

    static BackupFileNameBuilder objectTreeBackup() {
        return ObjectTreeBackupFileNameBuilder.getInstance();
    }

    /**
     * Build a backup file name based on given information.
     * @param time when the backup was created.
     * @param backupName the custom name of this backup. Note that this should be a valid file name in current file system.
     * @return the backup file name string.
     */
    String build(LocalDateTime time, String backupName);

}
