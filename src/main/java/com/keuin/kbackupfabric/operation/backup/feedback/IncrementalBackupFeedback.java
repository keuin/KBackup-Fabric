package com.keuin.kbackupfabric.operation.backup.feedback;

import com.keuin.kbackupfabric.backup.incremental.manager.IncCopyResult;
import org.jetbrains.annotations.Nullable;

public class IncrementalBackupFeedback implements BackupFeedback {
    private final boolean success;
    private final IncCopyResult copyResult;

    public IncrementalBackupFeedback(boolean success, @Nullable IncCopyResult copyResult) {
        this.success = success;
        this.copyResult = copyResult;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    public IncCopyResult getCopyResult() {
        return copyResult;
    }

    @Override
    public String getFeedback() {
        if (success && copyResult != null)
            return copyResult.toString();
        else
            return "Backup failed.";
    }
}
