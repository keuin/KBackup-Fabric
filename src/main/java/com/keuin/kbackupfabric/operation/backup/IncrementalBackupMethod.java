package com.keuin.kbackupfabric.operation.backup;

import com.google.gson.JsonObject;
import com.keuin.kbackupfabric.util.PrintUtil;
import com.keuin.kbackupfabric.util.backup.builder.BackupFileNameBuilder;
import com.keuin.kbackupfabric.util.backup.builder.ObjectTreeBackupFileNameBuilder;
import com.keuin.kbackupfabric.util.backup.formatter.BackupFileNameFormatter;
import com.keuin.kbackupfabric.util.backup.formatter.ObjectTreeBackupFileNameFormatter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;

public class IncrementalBackupMethod implements BackupMethod {

    private static final IncrementalBackupMethod INSTANCE = new IncrementalBackupMethod();

    public static IncrementalBackupMethod getInstance() {
        return INSTANCE;
    }

    @Override
    public BackupResult backup(String backupName, String levelPath, String backupSaveDirectory) throws IOException {
        /*
        1. Analyze the save directory, to get a json containing md5 values of all files.
        2. Copy new files which we do not have in our backup repository.
        3. Save the above json as a backup file. When restoring from this,
           what we have to do is just copy all files back from the repository,
           based on their md5 digests.
         */

        boolean success = true;
        // Generate JSON
        JsonObject hashJson = IncrementalBackupUtil.generateDirectoryJsonObject(levelPath);
        // Copy files
        long newFilesSizeBytes = IncrementalBackupUtil.saveNewFiles(backupSaveDirectory, levelPath, hashJson);
        if(newFilesSizeBytes < 0) {
            success = false;
            PrintUtil.error("Failed to copy new files to object tree.");
        }
        // Save JSON tree
        File jsonFile = new File(String.valueOf(Paths.get(backupSaveDirectory, BackupFileNameBuilder.objectTreeBackup().build(LocalDateTime.now(), backupName))));
        // TODO
        return new BackupResult(success, newFilesSizeBytes);
    }

    @Override
    public boolean restore(String backupName, String levelPath, String backupSaveDirectory) throws IOException {
        return false;
    }

    @Override
    public BackupFileNameBuilder getBackupFileNameBuilder() {
        return null;
    }

    @Override
    public BackupFileNameFormatter getBackupFileNameFormatter() {
        return null;
    }
}
