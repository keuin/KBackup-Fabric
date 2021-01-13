package com.keuin.kbackupfabric.operation.backup.method;

import com.keuin.kbackupfabric.operation.backup.feedback.BackupFeedback;

import java.io.IOException;

/**
 * Provide specific backup method, which has been configured with proper settings,
 * such as saving directory and level path.
 */
public interface ConfiguredBackupMethod {

    /**
     * Perform a backup with given method. The backup will be saved as the given name.
     * Note: real file name depends on the backup type.
     *
     * @return backup result.
     */
    BackupFeedback backup() throws IOException;

    boolean restore() throws IOException;

    /**
     * Create backup save directory and do some essential initialization before the backup process.
     *
     * @return false if failed, then the backup process won't proceed.
     */
    boolean touch();

    /**
     * Get the used backup file name.
     *
     * @return the file name.
     */
    String getBackupFileName();

}
