package com.keuin.kbackupfabric.operation.backup.feedback;

import java.util.Objects;

public abstract class SuccessBackupFeedback implements BackupFeedback {

    private final String message;

    public SuccessBackupFeedback(String successMessage) {
        this.message = Objects.requireNonNull(successMessage);
    }

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public String getFeedback() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SuccessBackupFeedback that = (SuccessBackupFeedback) o;
        return message.equals(that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message);
    }
}
