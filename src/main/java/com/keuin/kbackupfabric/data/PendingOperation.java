package com.keuin.kbackupfabric.data;

public class PendingOperation {
    private final PendingOperationType operationType;
    private final String backupName;

    public PendingOperation(PendingOperationType operationType, String backupName) {
        this.operationType = operationType;
        this.backupName = backupName;
    }

    public static PendingOperation deleteOperation(String backupName) {
        return new PendingOperation(PendingOperationType.DELETE, backupName);
    }

    public static PendingOperation restoreOperation(String backupName) {
        return new PendingOperation(PendingOperationType.RESTORE_TO, backupName);
    }

    public boolean isDelete() {
        return operationType == PendingOperationType.DELETE;
    }

    public boolean isRestore() {
        return operationType == PendingOperationType.RESTORE_TO;
    }

    public String getBackupName() {
        return backupName;
    }

    @Override
    public String toString() {
        String op = "operation";
        if (isDelete())
            op = "deletion";
        if (isRestore())
            op = "restoration";
        return String.format("%s on backup %s", op, getBackupName());
    }
}
