package com.keuin.kbackupfabric.util.backup;

/**
 * Representing the backup type.
 * Should only be used in BackupFileNameBuilder and BackupFileNameFormatter
 */
public enum BackupType {

    PRIMITIVE_ZIP_BACKUP("Primitive Zip Backup"),
    OBJECT_TREE_BACKUP("Object Tree Backup");

    private final String name;
    BackupType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
