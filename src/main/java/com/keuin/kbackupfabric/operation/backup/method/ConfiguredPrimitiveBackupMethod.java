package com.keuin.kbackupfabric.operation.backup.method;

import com.keuin.kbackupfabric.exception.ZipUtilException;
import com.keuin.kbackupfabric.metadata.BackupMetadata;
import com.keuin.kbackupfabric.operation.backup.feedback.PrimitiveBackupFeedback;
import com.keuin.kbackupfabric.util.FilesystemUtil;
import com.keuin.kbackupfabric.util.PrintUtil;
import com.keuin.kbackupfabric.util.ZipUtil;
import com.keuin.kbackupfabric.util.backup.BackupFilesystemUtil;
import com.keuin.kbackupfabric.util.backup.BackupNameTimeFormatter;
import com.keuin.kbackupfabric.util.backup.name.PrimitiveBackupFileNameEncoder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import static org.apache.commons.io.FileUtils.forceDelete;

public class ConfiguredPrimitiveBackupMethod implements ConfiguredBackupMethod {

    private final String backupFileName;
    private final String levelPath;
    private final String backupSavePath;

    public ConfiguredPrimitiveBackupMethod(String backupFileName, String levelPath, String backupSavePath) {
        this.backupFileName = backupFileName;
        this.levelPath = levelPath;
        this.backupSavePath = backupSavePath;
    }

    @Deprecated
    private String getBackupFileName(LocalDateTime time, String backupName) {
        String timeString = BackupNameTimeFormatter.localDateTimeToString(time);
        return String.format("%s%s_%s%s", BackupFilesystemUtil.getBackupFileNamePrefix(), timeString, backupName, ".zip");
    }

    @Override
    public PrimitiveBackupFeedback backup() throws IOException {
        try {
            String customBackupName = new PrimitiveBackupFileNameEncoder().decode(backupFileName).customName;
            BackupMetadata backupMetadata = new BackupMetadata(System.currentTimeMillis(), customBackupName);
            PrintUtil.info(String.format("zip(srcPath=%s, destPath=%s)", levelPath, backupSavePath));
            PrintUtil.info("Compressing level ...");
            ZipUtil.makeBackupZip(levelPath, backupSavePath, backupFileName, backupMetadata);
        } catch (ZipUtilException exception) {
            PrintUtil.info("Infinite recursive of directory tree detected, backup was aborted.");
            return new PrimitiveBackupFeedback(false, 0);
        }

        // Get backup file size and return
        return new PrimitiveBackupFeedback(true, FilesystemUtil.getFileSizeBytes(backupSavePath, backupFileName));
    }

    @Override
    public boolean restore() throws IOException {
        // Delete old level
        PrintUtil.info("Server stopped. Deleting old level ...");
        File levelDirFile = new File(levelPath);
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
        ZipUtil.unzip(Paths.get(backupSavePath, backupFileName).toString(), levelPath, false);
        long endTime = System.currentTimeMillis();
        PrintUtil.info(String.format("Restore complete! (%.2fs) Please restart the server manually.", (endTime - startTime) / 1000.0));
        PrintUtil.info("If you want to restart automatically after restoring, please check the manual at: https://github.com/keuin/KBackup-Fabric/blob/master/README.md");

//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException ignored) {
//        }

        return true;
    }
}
