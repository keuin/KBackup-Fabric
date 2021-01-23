package com.keuin.kbackupfabric.backup.incremental.serializer;

import com.keuin.kbackupfabric.backup.incremental.ObjectCollection2;
import com.keuin.kbackupfabric.backup.name.BackupFileNameEncoder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * The old-style incremental backup. Just to keep backward compatibility with old backups.
 */
public class SavedIncBackupV0 implements SavedIncrementalBackup {

    private final ObjectCollection2 objectCollection2;
    private final String backupName;
    private final LocalDateTime namedBackupTime;

    public SavedIncBackupV0(ObjectCollection2 objectCollection2, BackupFileNameEncoder.BackupBasicInformation backupBasicInformation) {
        Objects.requireNonNull(objectCollection2);
        Objects.requireNonNull(backupBasicInformation);

        this.objectCollection2 = objectCollection2;
        this.backupName = backupBasicInformation.customName;
        this.namedBackupTime = backupBasicInformation.time;
    }


    @Override
    public ObjectCollection2 getObjectCollection() {
        return objectCollection2;
    }

    @Override
    public String getBackupName() {
        return backupName;
    }

    @Override
    public ZonedDateTime getBackupTime() {
        return namedBackupTime.atZone(ZoneId.systemDefault());
    }

    @Override
    public int getFilesAdded() {
        return -1; // missing info
    }

    @Override
    public long getTotalSizeBytes() {
        return -1; // missing info
    }

    @Override
    public long getIncreasedSizeBytes() {
        return -1; // missing info
    }

    @Override
    public String toString() {
        return String.format("(Legacy Backup) %s, created at %s", backupName, namedBackupTime);
    }
}
