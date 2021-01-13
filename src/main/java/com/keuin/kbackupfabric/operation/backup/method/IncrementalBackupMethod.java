package com.keuin.kbackupfabric.operation.backup.method;

import com.keuin.kbackupfabric.operation.backup.feedback.IncrementalBackupFeedback;

import java.io.IOException;

public class IncrementalBackupMethod implements BackupMethod {
    @Override
    public IncrementalBackupFeedback backup(String customBackupName, String levelPath, String backupSaveDirectory) throws IOException {
        return null;
    }

    @Override
    public boolean restore(String backupName, String levelPath, String backupSaveDirectory) throws IOException {
        return false;
    }
}
