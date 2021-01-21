package com.keuin.kbackupfabric.operation.backup.method;

import com.keuin.kbackupfabric.backup.incremental.ObjectCollection;
import com.keuin.kbackupfabric.backup.incremental.ObjectCollectionFactory;
import com.keuin.kbackupfabric.backup.incremental.ObjectCollectionSerializer;
import com.keuin.kbackupfabric.backup.incremental.identifier.Sha256Identifier;
import com.keuin.kbackupfabric.backup.incremental.manager.IncrementalBackupStorageManager;
import com.keuin.kbackupfabric.operation.backup.feedback.IncrementalBackupFeedback;
import com.keuin.kbackupfabric.util.FilesystemUtil;
import com.keuin.kbackupfabric.util.PrintUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Logger;

public class ConfiguredIncrementalBackupMethod implements ConfiguredBackupMethod {

    private final String backupIndexFileName;
    private final String levelPath;
    private final String backupIndexFileSaveDirectory;
    private final String backupBaseDirectory;

    private static final Logger LOGGER = Logger.getLogger(ConfiguredIncrementalBackupMethod.class.getName());

    public ConfiguredIncrementalBackupMethod(String backupIndexFileName, String levelPath, String backupIndexFileSaveDirectory, String backupBaseDirectory) {
        this.backupIndexFileName = backupIndexFileName;
        this.levelPath = levelPath;
        this.backupIndexFileSaveDirectory = backupIndexFileSaveDirectory;
        this.backupBaseDirectory = backupBaseDirectory;
    }

    @Override
    public IncrementalBackupFeedback backup() {
        IncrementalBackupFeedback feedback;
        try {
            File levelPathFile = new File(levelPath);

            // construct incremental backup index
            PrintUtil.info("Hashing files...");
            ObjectCollection collection = new ObjectCollectionFactory<>(Sha256Identifier.getFactory())
                    .fromDirectory(levelPathFile, new HashSet<>(Arrays.asList("session.lock", "kbackup_metadata")));

            // update storage
            PrintUtil.info("Copying files...");
            IncrementalBackupStorageManager storageManager = new IncrementalBackupStorageManager(Paths.get(backupBaseDirectory));
            int filesAdded = storageManager.addObjectCollection(collection, levelPathFile);

            // save index file
            PrintUtil.info("Saving index file...");
            ObjectCollectionSerializer.toFile(collection, new File(backupIndexFileSaveDirectory, backupIndexFileName));

            // return result
            PrintUtil.info("Incremental backup finished.");
            feedback = new IncrementalBackupFeedback(filesAdded >= 0, filesAdded);
        } catch (IOException e) {
            feedback = new IncrementalBackupFeedback(false, 0);
        }

        if (!feedback.isSuccess()) {
            // do clean-up if failed
            File backupIndexFile = new File(backupIndexFileSaveDirectory, backupIndexFileName);
            if (backupIndexFile.exists()) {
                if (!backupIndexFile.delete()) {
                    LOGGER.warning("Failed to clean up: cannot delete file " + backupIndexFile.getName());
                }
            }

            //TODO: do more deep clean for object files
        }

        return feedback;
    }

    @Override
    public boolean restore() throws IOException {
        // load collection
        PrintUtil.info("Loading file list...");
        ObjectCollection collection = ObjectCollectionSerializer.fromFile(
                new File(backupIndexFileSaveDirectory, backupIndexFileName)
        );

        // delete old level
        File levelPathFile = new File(levelPath);
        PrintUtil.info("Deleting old level...");
        if (!FilesystemUtil.forceDeleteDirectory(levelPathFile)) {
            PrintUtil.info("Failed to delete old level!");
            return false;
        }

        // restore file
        PrintUtil.info("Copying files...");
        IncrementalBackupStorageManager storageManager = new IncrementalBackupStorageManager(Paths.get(backupBaseDirectory));
        int restoreObjectCount = storageManager.restoreObjectCollection(collection, levelPathFile);

        PrintUtil.info(String.format("%d file(s) restored.", restoreObjectCount));
        return true;
    }

    @Override
    public boolean touch() {
        File baseDirectoryFile = new File(backupBaseDirectory);
        return baseDirectoryFile.isDirectory() || baseDirectoryFile.mkdir();
    }

    @Override
    public String getBackupFileName() {
        return backupIndexFileName;
    }


}
