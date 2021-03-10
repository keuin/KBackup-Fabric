package com.keuin.kbackupfabric.operation.backup.feedback;

import com.keuin.kbackupfabric.backup.incremental.manager.IncCopyResult;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class IncrementalBackupFeedback implements BackupFeedback {
    private final boolean success;
    private final IncCopyResult copyResult;
    // if the backup failed because of an exception, set this.
    // Otherwise, this should be null.
    private final Throwable throwable;

    public IncrementalBackupFeedback(boolean success, @Nullable IncCopyResult copyResult) {
        this.success = success;
        this.copyResult = copyResult;
        this.throwable = null;
    }

    /**
     * Create a failed backup feedback caused by an exception.
     *
     * @param t the exception.
     */
    public IncrementalBackupFeedback(Throwable t) {
        Objects.requireNonNull(t);
        this.success = false;
        this.copyResult = null;
        this.throwable = t;
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
            return (throwable == null) ? "No further information." : (throwable.getLocalizedMessage());
    }
}
