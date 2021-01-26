package com.keuin.kbackupfabric.backup.incremental.manager;

import com.keuin.kbackupfabric.backup.BackupFilesystemUtil;

import java.util.Objects;

/**
 * Returned by `addObjectCollection` in IncrementalBackupStorageManager.
 * Immutable.
 */
public class IncCopyResult {

    private final int totalFiles;
    private final int filesCopied;
    private final long bytesCopied;
    private final long bytesTotal;

    public static final IncCopyResult ZERO = new IncCopyResult(0, 0, 0, 0);

    public IncCopyResult(int totalFiles, int filesCopied, long bytesCopied, long bytesTotal) {
        this.totalFiles = totalFiles;
        this.filesCopied = filesCopied;
        this.bytesCopied = bytesCopied;
        this.bytesTotal = bytesTotal;
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
    public int getFilesCopied() {
        return filesCopied;
    }

    /**
     * Get total bytes of new files added to the base.
     *
     * @return bytes.
     */
    public long getBytesCopied() {
        return bytesCopied;
    }

    /**
     * Get total bytes of all files in the collection. This equals to copied_files_bytes + reused_files_bytes.
     *
     * @return bytes.
     */
    public long getBytesTotal() {
        return bytesTotal;
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
                filesCopied + a.filesCopied,
                bytesCopied + a.bytesCopied,
                bytesTotal + a.bytesTotal
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IncCopyResult that = (IncCopyResult) o;
        return totalFiles == that.totalFiles &&
                filesCopied == that.filesCopied &&
                bytesCopied == that.bytesCopied &&
                bytesTotal == that.bytesTotal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalFiles, filesCopied, bytesCopied, bytesTotal);
    }

    @Override
    public String toString() {
        return String.format(
                "File(s) added: %d (%s in size, totally %d files). Total backup-ed files size: %s",
                filesCopied,
                BackupFilesystemUtil.getFriendlyFileSizeString(bytesCopied),
                totalFiles,
                BackupFilesystemUtil.getFriendlyFileSizeString(bytesTotal)
        );
    }
}
