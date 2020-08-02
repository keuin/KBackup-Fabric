package com.keuin.kbackupfabric.operation.backup;

import com.keuin.kbackupfabric.exception.ZipUtilException;
import com.keuin.kbackupfabric.metadata.BackupMetadata;
import com.keuin.kbackupfabric.util.PrintUtil;
import com.keuin.kbackupfabric.util.ZipUtil;
import com.keuin.kbackupfabric.util.backup.builder.BackupFileNameBuilder;
import com.keuin.kbackupfabric.util.backup.formatter.BackupFileNameFormatter;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PrimitiveBackupMehtod implements BackupMethod {
    @Override
    public boolean backup(String backupName, String levelPath, String backupSaveDirectory) throws IOException {
        try {
            String backupFileName = BackupFileNameBuilder.primitiveZipBackup().build(LocalDateTime.now(),backupName);
            BackupMetadata backupMetadata = new BackupMetadata(System.currentTimeMillis(), backupName);

            PrintUtil.info(String.format("zip(srcPath=%s, destPath=%s)", levelPath, backupSaveDirectory));
            PrintUtil.info("Compressing level ...");
            ZipUtil.makeBackupZip(levelPath, backupSaveDirectory, backupFileName, backupMetadata);

        } catch (ZipUtilException exception) {
            PrintUtil.info("Infinite recursive of directory tree detected, backup was aborted.");
            return false;
        }
        return true;
    }

    @Override
    public BackupFileNameBuilder getBackupFileNameBuilder() {
        return BackupFileNameBuilder.primitiveZipBackup();
    }

    @Override
    public BackupFileNameFormatter getBackupFileNameFormatter() {
        return BFNF;
    }
}
