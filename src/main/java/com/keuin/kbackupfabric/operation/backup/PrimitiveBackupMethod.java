package com.keuin.kbackupfabric.operation.backup;

import com.keuin.kbackupfabric.exception.ZipUtilException;
import com.keuin.kbackupfabric.metadata.BackupMetadata;
import com.keuin.kbackupfabric.util.FilesystemUtil;
import com.keuin.kbackupfabric.util.PrintUtil;
import com.keuin.kbackupfabric.util.ZipUtil;
import com.keuin.kbackupfabric.util.backup.builder.BackupFileNameBuilder;
import com.keuin.kbackupfabric.util.backup.formatter.BackupFileNameFormatter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import static org.apache.commons.io.FileUtils.forceDelete;

public class PrimitiveBackupMethod implements BackupMethod {

    private static final PrimitiveBackupMethod INSTANCE = new PrimitiveBackupMethod();
    private static int zipLevel = 9;

    public static PrimitiveBackupMethod getInstance() {
        return INSTANCE;
    }

    @Override
    public BackupResult backup(String backupName, String levelPath, String backupSaveDirectory) throws IOException {
        String backupFileName = BackupFileNameBuilder.primitiveZipBackup().build(LocalDateTime.now(),backupName);
        try {
            BackupMetadata backupMetadata = new BackupMetadata(System.currentTimeMillis(), backupName);

            PrintUtil.info(String.format("zip(srcPath=%s, destPath=%s)", levelPath, backupSaveDirectory));
            PrintUtil.info("Compressing level ...");
            ZipUtil.makeBackupZip(levelPath, backupSaveDirectory, backupFileName, backupMetadata, zipLevel);

        } catch (ZipUtilException exception) {
            PrintUtil.info("Infinite recursive of directory tree detected, backup was aborted.");
            return new BackupResult(false, 0);
        }

        // Get backup file size and return
        return new BackupResult(true, FilesystemUtil.getFileSizeBytes(backupSaveDirectory, backupFileName));
    }

    @Override
    public boolean restore(String backupName, String levelDirectory, String backupSaveDirectory) throws IOException {
        // Delete old level
        PrintUtil.info("Server stopped. Deleting old level ...");
        File levelDirFile = new File(levelDirectory);
        long startTime = System.currentTimeMillis();

        int failedCounter = 0;
        final int MAX_RETRY_TIMES = 20;
        while (failedCounter < MAX_RETRY_TIMES) {
            System.gc();
            if (!levelDirFile.delete() && levelDirFile.exists()) {
                System.gc();
                forceDelete(levelDirFile); // Try to force delete.
            }
            if (!levelDirFile.exists())
                break;
            ++failedCounter;
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
        }
        if (levelDirFile.exists()) {
            PrintUtil.error(String.format("Cannot restore: failed to delete old level %s .", levelDirFile.getName()));
            return false;
        }

        // TODO: Refactor this to the concrete BackupMethod.
        // Decompress archive
        PrintUtil.info("Decompressing archived level ...");
        ZipUtil.unzip(Paths.get(backupSaveDirectory, backupName).toString(), levelDirectory, false);
        long endTime = System.currentTimeMillis();
        PrintUtil.info(String.format("Restore complete! (%.2fs) Please restart the server manually.", (endTime - startTime) / 1000.0));
        PrintUtil.info("If you want to restart automatically after restoring, please visit the project manual at: https://github.com/keuin/KBackup-Fabric/blob/master/README.md");

//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException ignored) {
//        }

        return true;
    }

    @Override
    public BackupFileNameBuilder getBackupFileNameBuilder() {
        return BackupFileNameBuilder.primitiveZipBackup();
    }

    @Override
    public BackupFileNameFormatter getBackupFileNameFormatter() {
        return BackupFileNameFormatter.primitiveZipBackup();
    }
}
