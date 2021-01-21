package com.keuin.kbackupfabric.operation.backup.feedback;

public abstract class FailedBackupFeedback implements BackupFeedback {

    private final String message;

    public FailedBackupFeedback(String message) {
        this.message = message;
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public String getFeedback() {
        return message;
    }
}
