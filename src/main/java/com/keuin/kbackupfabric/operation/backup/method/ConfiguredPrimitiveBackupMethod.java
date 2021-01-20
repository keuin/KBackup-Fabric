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
        if (!FilesystemUtil.forceDeleteDirectory(new File(levelPath))) {
            PrintUtil.info("Failed to delete old level!");
            return false;
        }


        // TODO: Refactor this to the concrete BackupMethod.
        // Decompress archive
        PrintUtil.info("Decompressing archived level ...");
        ZipUtil.unzip(Paths.get(backupSavePath, backupFileName).toString(), levelPath, false);


//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException ignored) {
//        }

        return true;
    }

    @Override
    public boolean touch() {
        File backupSaveDirectoryFile = new File(backupSavePath);
        return backupSaveDirectoryFile.isDirectory() || backupSaveDirectoryFile.mkdir();
    }

    @Override
    public String getBackupFileName() {
        return backupFileName;
    }

}
