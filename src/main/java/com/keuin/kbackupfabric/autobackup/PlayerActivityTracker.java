package com.keuin.kbackupfabric.autobackup;

public interface PlayerActivityTracker {
    /**
     * Update the checkpoint, return accumulated result.
     *
     * @return if there is at least one player logged in since last checkpoint.
     */
    boolean getCheckpoint();

    /**
     * Mark dirty. In the next checkpoint, the backup will be performed.
     */
    void setCheckpoint();
}
