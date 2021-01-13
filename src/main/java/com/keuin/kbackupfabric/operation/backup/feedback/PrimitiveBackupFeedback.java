package com.keuin.kbackupfabric.operation.backup.feedback;

import static com.keuin.kbackupfabric.util.backup.BackupFilesystemUtil.getFriendlyFileSizeString;

public class PrimitiveBackupFeedback implements BackupFeedback {
    private final boolean success;
    private final long backupSizeBytes;

    public PrimitiveBackupFeedback(boolean success, long backupSizeBytes) {
        this.success = success;
        this.backupSizeBytes = backupSizeBytes;
    }

    public boolean isSuccess() {
        return success;
    }

    public long getBackupSizeBytes() {
        return backupSizeBytes;
    }

    @Override
    public String getFeedback() {
        if (success && backupSizeBytes >= 0)
            return String.format(" File size: %s.", getFriendlyFileSizeString(backupSizeBytes));
        else
            return "";
    }
}
