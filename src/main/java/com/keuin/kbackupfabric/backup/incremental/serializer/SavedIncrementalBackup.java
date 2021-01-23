package com.keuin.kbackupfabric.backup.incremental.serializer;


import com.keuin.kbackupfabric.backup.incremental.ObjectCollection2;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * The abstraction of an object saved in the disk, containing all information (except binary data of files) about an incremental backup.
 */
public interface SavedIncrementalBackup extends Serializable {

    /**
     * Get an instance with the latest version.
     */
    static SavedIncrementalBackup newLatest(ObjectCollection2 objectCollection2, String backupName, ZonedDateTime backupTime, long totalSizeBytes, long increasedSizeBytes, int filesAdded, int totalFiles) {
        return new SavedIncBackupV1(objectCollection2, backupName, backupTime, totalSizeBytes, increasedSizeBytes, filesAdded, totalFiles);
    }

    /**
     * Get the object collection of the level directory.
     *
     * @return the object collection.
     */
    ObjectCollection2 getObjectCollection();

    /**
     * Get the custom backup name.
     *
     * @return the backup name.
     */
    String getBackupName();

    /**
     * Get the time when this backup was made.
     *
     * @return the time.
     */
    ZonedDateTime getBackupTime();

    /**
     * Get new files added to the base.
     *
     * @return file count.
     */
    int getFilesAdded();

    /**
     * Get the total size of the saved world.
     *
     * @return the size in bytes.
     */
    long getTotalSizeBytes();

    /**
     * Get the size we cost to add this backup into the base.
     *
     * @return the increased size in bytes.
     */
    long getIncreasedSizeBytes();
}
