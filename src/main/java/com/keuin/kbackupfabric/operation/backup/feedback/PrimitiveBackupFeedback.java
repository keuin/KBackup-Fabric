package com.keuin.kbackupfabric.operation.backup.feedback;

import static com.keuin.kbackupfabric.backup.BackupFilesystemUtil.getFriendlyFileSizeString;

public class PrimitiveBackupFeedback implements BackupFeedback {

    private final boolean success;
    private final long backupSizeBytes; // if success==false, this is invalid
    private final String message; // if success==true, this is invalid

    private PrimitiveBackupFeedback(boolean success, long backupSizeBytes, String message) {
        this.success = success;
        this.backupSizeBytes = backupSizeBytes;
        this.message = message;
    }

    public static PrimitiveBackupFeedback createSuccessFeedback(long backupSizeBytes) {
        return new PrimitiveBackupFeedback(true, backupSizeBytes, "");
    }

    public static PrimitiveBackupFeedback createFailFeedback(String message) {
        return new PrimitiveBackupFeedback(false, -1, message);
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    public long getBackupSizeBytes() {
        return backupSizeBytes;
    }

    @Override
    public String getFeedback() {
        if (success && backupSizeBytes >= 0)
            return String.format("Backup file size: %s.", getFriendlyFileSizeString(backupSizeBytes));
        else
            return message;
    }
}
