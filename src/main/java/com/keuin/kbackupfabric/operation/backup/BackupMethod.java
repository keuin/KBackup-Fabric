package com.keuin.kbackupfabric.operation.backup;

import com.keuin.kbackupfabric.util.backup.builder.BackupFileNameBuilder;
import com.keuin.kbackupfabric.util.backup.formatter.BackupFileNameFormatter;

import java.io.IOException;

/**
 * Provide specific backup method, which is implemented statelessly.
 */
public interface BackupMethod {

    /**
     * Perform a backup with given method. The backup will be saved as the given name.
     * Note: real file name depends on the backup type.
     * @param backupName the backup name.
     * @return if the backup operation succeed.
     */
    BackupResult backup(String backupName, String levelPath, String backupSaveDirectory) throws IOException;

    boolean restore(String backupName, String levelPath, String backupSaveDirectory) throws IOException;

    BackupFileNameBuilder getBackupFileNameBuilder();

    BackupFileNameFormatter getBackupFileNameFormatter();

    class BackupResult {
        private final boolean success;
        private final long backupSizeBytes;

        public BackupResult(boolean success, long backupSizeBytes) {
            this.success = success;
            this.backupSizeBytes = backupSizeBytes;
        }

        public boolean isSuccess() {
            return success;
        }

        public long getBackupSizeBytes() {
            return backupSizeBytes;
        }
    }
}
