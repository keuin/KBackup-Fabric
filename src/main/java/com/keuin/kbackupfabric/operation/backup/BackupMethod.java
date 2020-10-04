package com.keuin.kbackupfabric.operation.backup;

import com.keuin.kbackupfabric.util.backup.builder.BackupFileNameBuilder;
import com.keuin.kbackupfabric.util.backup.formatter.BackupFileNameFormatter;
import com.sun.istack.internal.NotNull;

import java.io.IOException;

public interface BackupMethod {

    /**
     * Perform a backup with given method. The backup will be saved as the given name.
     * Note: real file name depends on the backup type.
     * @param backupName the backup name.
     * @return if the backup operation succeed.
     */
    boolean backup(@NotNull String backupName, @NotNull String levelPath, @NotNull String backupSaveDirectory) throws IOException;

    BackupFileNameBuilder getBackupFileNameBuilder();

    BackupFileNameFormatter getBackupFileNameFormatter();

}
