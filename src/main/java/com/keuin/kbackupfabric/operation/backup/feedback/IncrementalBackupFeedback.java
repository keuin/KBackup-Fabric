package com.keuin.kbackupfabric.operation.backup.feedback;

public class IncrementalBackupFeedback implements BackupFeedback {
    private final boolean success;
    private final int newFilesAdded;

    public IncrementalBackupFeedback(boolean success, int newFilesAdded) {
        this.success = success;
        this.newFilesAdded = newFilesAdded;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    public long getNewFilesAdded() {
        return newFilesAdded;
    }

    @Override
    public String getFeedback() {
        if (success && newFilesAdded >= 0)
            return String.format("File(s) added: %d.", newFilesAdded);
        else
            return "";
    }
}
