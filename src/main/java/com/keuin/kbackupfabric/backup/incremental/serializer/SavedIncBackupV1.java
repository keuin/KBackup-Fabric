package com.keuin.kbackupfabric.backup.incremental.serializer;

import com.keuin.kbackupfabric.backup.BackupFilesystemUtil;
import com.keuin.kbackupfabric.backup.incremental.ObjectCollection2;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class SavedIncBackupV1 implements SavedIncrementalBackup, Serializable {

    private final ObjectCollection2 objectCollection2;
    private final String backupName;
    private final ZonedDateTime backupTime;
    private final long totalSizeBytes;
    private final long increasedSizeBytes;
    private final int filesAdded;
    private final int totalFiles;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    public SavedIncBackupV1(ObjectCollection2 objectCollection2, String backupName, ZonedDateTime backupTime, long totalSizeBytes, long increasedSizeBytes, int filesAdded, int totalFiles) {
        this.totalFiles = totalFiles;
        Objects.requireNonNull(objectCollection2);
        Objects.requireNonNull(backupName);
        Objects.requireNonNull(backupTime);
        this.objectCollection2 = objectCollection2;
        this.backupName = backupName;
        this.backupTime = backupTime;
        this.totalSizeBytes = totalSizeBytes;
        this.increasedSizeBytes = increasedSizeBytes;
        this.filesAdded = filesAdded;
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
        return backupTime;
    }

    @Override
    public int getFilesAdded() {
        return filesAdded;
    }

    @Override
    public long getTotalSizeBytes() {
        return totalSizeBytes;
    }

    @Override
    public long getIncreasedSizeBytes() {
        return increasedSizeBytes;
    }

    public int getTotalFiles() {
        return totalFiles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SavedIncBackupV1 that = (SavedIncBackupV1) o;
        return totalSizeBytes == that.totalSizeBytes &&
                increasedSizeBytes == that.increasedSizeBytes &&
                filesAdded == that.filesAdded &&
                totalFiles == that.totalFiles &&
                objectCollection2.equals(that.objectCollection2) &&
                backupName.equals(that.backupName) &&
                backupTime.equals(that.backupTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectCollection2, backupName, backupTime, totalSizeBytes, increasedSizeBytes, filesAdded, totalFiles);
    }

    @Override
    public String toString() {
        return String.format(
                "%s, created at %s, files: %d (total size: %s), copied size: %s, files added: %d",
                backupName,
                backupTime.format(formatter),
                totalFiles,
                BackupFilesystemUtil.getFriendlyFileSizeString(totalSizeBytes),
                BackupFilesystemUtil.getFriendlyFileSizeString(increasedSizeBytes),
                filesAdded
        );
    }
}
