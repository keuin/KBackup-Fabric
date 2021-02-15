package com.keuin.kbackupfabric.backup.incremental.manager;

import com.keuin.kbackupfabric.backup.BackupFilesystemUtil;

import java.util.Objects;

/**
 * Returned by `addObjectCollection` in IncrementalBackupStorageManager.
 * Immutable.
 */
public class IncCopyResult {

    private final int totalFiles;
    private final int copiedFiles;
    private final long copiedBytes;
    private final long totalBytes;

    public static final IncCopyResult ZERO = new IncCopyResult(0, 0, 0, 0);

    public IncCopyResult(int totalFiles, int copiedFiles, long copiedBytes, long totalBytes) {
        this.totalFiles = totalFiles;
        this.copiedFiles = copiedFiles;
        this.copiedBytes = copiedBytes;
        this.totalBytes = totalBytes;
    }

    /**
     * Get total files in the collection, containing reused files.
     *
     * @return file count.
     */
    public int getTotalFiles() {
        return totalFiles;
    }

    /**
     * Get new files added to the base.
     *
     * @return file count.
     */
    public int getCopiedFiles() {
        return copiedFiles;
    }

    /**
     * Get total bytes of new files added to the base.
     *
     * @return bytes.
     */
    public long getCopiedBytes() {
        return copiedBytes;
    }

    /**
     * Get total bytes of all files in the collection. This equals to copied_files_bytes + reused_files_bytes.
     *
     * @return bytes.
     */
    public long getTotalBytes() {
        return totalBytes;
    }

    /**
     * Add with another AddResult.
     *
     * @param a object.
     * @return the add result.
     */
    public IncCopyResult addWith(IncCopyResult a) {
        Objects.requireNonNull(a);
        return new IncCopyResult(
                totalFiles + a.totalFiles,
                copiedFiles + a.copiedFiles,
                copiedBytes + a.copiedBytes,
                totalBytes + a.totalBytes
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IncCopyResult that = (IncCopyResult) o;
        return totalFiles == that.totalFiles &&
                copiedFiles == that.copiedFiles &&
                copiedBytes == that.copiedBytes &&
                totalBytes == that.totalBytes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalFiles, copiedFiles, copiedBytes, totalBytes);
    }

    @Override
    public String toString() {
        return String.format(
                "File(s) added: %d (%s in size, totally %d files). Total backup-ed files size: %s (%.2f%% reused)",
                copiedFiles,
                BackupFilesystemUtil.getFriendlyFileSizeString(copiedBytes),
                totalFiles,
                BackupFilesystemUtil.getFriendlyFileSizeString(totalBytes),
                (1 - 1.0f * copiedBytes / totalBytes) * 100
        );
    }
}
