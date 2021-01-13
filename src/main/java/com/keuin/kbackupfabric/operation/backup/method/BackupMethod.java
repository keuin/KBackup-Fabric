package com.keuin.kbackupfabric.operation.backup.method;

import com.keuin.kbackupfabric.operation.backup.feedback.BackupFeedback;

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
    BackupFeedback backup(String backupName, String levelPath, String backupSaveDirectory) throws IOException;

    boolean restore(String backupName, String levelPath, String backupSaveDirectory) throws IOException;

}
