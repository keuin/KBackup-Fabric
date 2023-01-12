package com.keuin.kbackupfabric.ui;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Holding all types of backups for the user interaction logic.
 */
public class BackupManager {

    private final File backupStorageDirectory;

    public BackupManager(File backupStorageDirectory) {
        this.backupStorageDirectory = backupStorageDirectory;
    }

    public BackupManager(String backupStorageDirectory) {
        this.backupStorageDirectory = new File(backupStorageDirectory);
    }

    /**
     * Get available backups in the disk.
     *
     * @return all backups.
     */
    public Iterable<BackupInfo> getAllBackups() {
        return () -> {
            if (backupStorageDirectory.exists()) {
                if (!backupStorageDirectory.isDirectory()) {
                    throw new RuntimeException("Backup directory is not a directory.");
                }
            } else {
                if (!backupStorageDirectory.mkdirs()) {
                    throw new RuntimeException("Backup directory does not exist and cannot be created.");
                }
            }
            File[] backupFiles = backupStorageDirectory.listFiles();
            if (backupFiles == null) {
                throw new RuntimeException("Cannot list files in backup directory.");
            }

            return new Iterator<BackupInfo>() {
                private final Iterator<File> fileIterator = Arrays.stream(backupFiles).filter(file -> {
                    String name = file.getName().toLowerCase();
                    return name.endsWith(".zip") || name.endsWith(".kbi");
                }).iterator();

                @Override
                public boolean hasNext() {
                    return fileIterator.hasNext();
                }

                @Override
                public BackupInfo next() {
                    try {
                        File backupFile = fileIterator.next();
                        String fileName = backupFile.getName().toLowerCase();
                        if (fileName.endsWith(".zip"))
                            return PrimitiveBackupInfo.fromFile(backupFile);
                        if (fileName.endsWith(".kbi"))
                            return IncrementalBackupInfo.fromFile(backupFile);
                        throw new RuntimeException("Invalid backup file extname");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        };
    }
}
