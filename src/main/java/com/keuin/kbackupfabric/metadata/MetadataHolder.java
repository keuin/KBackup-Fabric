package com.keuin.kbackupfabric.metadata;

/**
 * In the first startup after restoring from a previous backup, the metadata is stored in this class.
 * The setMetadata can only be called when startup
 */
public class MetadataHolder {
    private static BackupMetadata metadata = null;

    public static BackupMetadata getMetadata() {
        return metadata;
    }

    public static void setMetadata(BackupMetadata metadata) {
        if (MetadataHolder.metadata == null)
            MetadataHolder.metadata = metadata;
    }

    public static boolean hasMetadata() {
        return MetadataHolder.metadata != null;
    }
}
