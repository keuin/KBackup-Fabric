package com.keuin.kbackupfabric.util.backup;

/**
 * Representing the backup type.
 * Should only be used in BackupFileNameBuilder and BackupFileNameFormatter
 */
@Deprecated
public enum BackupType {

    PRIMITIVE_ZIP_BACKUP("Primitive Zip Backup", "zip"),
    OBJECT_TREE_BACKUP("Object Tree Backup", "incremental");

    private final String friendlyName; // e.g. Primitive Zip Backup
    private final String name; // e.g. zip

    BackupType(String friendlyName, String name) {
        this.friendlyName = friendlyName;
        this.name = name;
    }

    /**
     * Get name used in command.
     * @return name (such as "zip", "incremental").
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return friendlyName;
    }
}
