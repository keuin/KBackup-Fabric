package com.keuin.kbackupfabric.operation.backup.method;

import com.keuin.kbackupfabric.operation.backup.feedback.IncrementalBackupFeedback;
import com.keuin.kbackupfabric.util.backup.incremental.ObjectCollection;
import com.keuin.kbackupfabric.util.backup.incremental.ObjectCollectionFactory;
import com.keuin.kbackupfabric.util.backup.incremental.ObjectCollectionSerializer;
import com.keuin.kbackupfabric.util.backup.incremental.identifier.Sha256Identifier;
import com.keuin.kbackupfabric.util.backup.incremental.manager.IncrementalBackupStorageManager;
import com.keuin.kbackupfabric.util.backup.name.IncrementalBackupFileNameEncoder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;

public class ConfiguredIncrementalBackupMethod implements ConfiguredBackupMethod {

    private final String backupFileName;
    private final String levelPath;
    private final String backupSavePath;

    public ConfiguredIncrementalBackupMethod(String backupFileName, String levelPath, String backupSavePath) {
        this.backupFileName = backupFileName;
        this.levelPath = levelPath;
        this.backupSavePath = backupSavePath;
    }

    @Override
    public IncrementalBackupFeedback backup() throws IOException {
        String customBackupName = new IncrementalBackupFileNameEncoder().decode(backupFileName).customName;
        String backupIndexFileName = new IncrementalBackupFileNameEncoder().encode(customBackupName, LocalDateTime.now());
        File levelPathFile = new File(levelPath);

        // construct incremental backup index
        ObjectCollection collection = new ObjectCollectionFactory<>(Sha256Identifier.getFactory())
                .fromDirectory(levelPathFile);

        // update storage
        IncrementalBackupStorageManager storageManager = new IncrementalBackupStorageManager(Paths.get(backupSavePath));
        int filesAdded = storageManager.addObjectCollection(collection, levelPathFile);

        // save index file
        ObjectCollectionSerializer.toFile(collection, new File(backupSavePath, backupIndexFileName));

        return new IncrementalBackupFeedback(filesAdded >= 0, filesAdded);
    }

    @Override
    public boolean restore() throws IOException {
        return false;
    }
}
