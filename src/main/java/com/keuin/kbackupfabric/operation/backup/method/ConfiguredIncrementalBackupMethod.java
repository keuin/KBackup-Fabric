package com.keuin.kbackupfabric.operation.backup.method;

import com.keuin.kbackupfabric.backup.incremental.ObjectCollection2;
import com.keuin.kbackupfabric.backup.incremental.ObjectCollectionFactory;
import com.keuin.kbackupfabric.backup.incremental.identifier.Sha256Identifier;
import com.keuin.kbackupfabric.backup.incremental.manager.IncCopyResult;
import com.keuin.kbackupfabric.backup.incremental.manager.IncrementalBackupStorageManager;
import com.keuin.kbackupfabric.backup.incremental.serializer.IncBackupInfoSerializer;
import com.keuin.kbackupfabric.backup.incremental.serializer.SavedIncrementalBackup;
import com.keuin.kbackupfabric.backup.name.BackupFileNameEncoder;
import com.keuin.kbackupfabric.backup.name.IncrementalBackupFileNameEncoder;
import com.keuin.kbackupfabric.operation.backup.feedback.IncrementalBackupFeedback;
import com.keuin.kbackupfabric.util.FilesystemUtil;
import com.keuin.kbackupfabric.util.PrintUtil;
import com.keuin.kbackupfabric.util.ThreadingUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.ZoneId;
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
        final int hashFactoryThreads = ThreadingUtil.getRecommendedThreadCount(); // how many threads do we use to generate the hash tree
        LOGGER.info("Threads: " + hashFactoryThreads);

        IncrementalBackupFeedback feedback;
        try {
            File levelPathFile = new File(levelPath);

            // construct incremental backup index
            PrintUtil.info("Hashing files...");
            // TODO
            ObjectCollection2 collection = new ObjectCollectionFactory<>(Sha256Identifier.getFactory(), hashFactoryThreads, 16)
                    .fromDirectory(levelPathFile, new HashSet<>(Arrays.asList("session.lock", "kbackup_metadata")));

            // update storage
            PrintUtil.info("Copying files...");
            IncrementalBackupStorageManager storageManager = new IncrementalBackupStorageManager(Paths.get(backupBaseDirectory));
            IncCopyResult copyResult = storageManager.addObjectCollection(collection, levelPathFile);
            if (copyResult == null) {
                PrintUtil.info("Failed to backup. No further information.");
                return new IncrementalBackupFeedback(false, null);
            }

            // save index file
            PrintUtil.info("Saving index file...");

            // legacy index file
//            ObjectCollectionSerializer.toFile(collection, new File(backupIndexFileSaveDirectory, backupIndexFileName));

            // newer saved info (with metadata)
            File indexFile = new File(backupIndexFileSaveDirectory, backupIndexFileName);
            BackupFileNameEncoder.BackupBasicInformation info = new IncrementalBackupFileNameEncoder().decode(backupIndexFileName);
            IncBackupInfoSerializer.toFile(indexFile, SavedIncrementalBackup.newLatest(
                    collection,
                    info.customName,
                    info.time.atZone(ZoneId.systemDefault()),
                    copyResult.getBytesTotal(),
                    copyResult.getBytesCopied(),
                    copyResult.getFilesCopied(),
                    copyResult.getTotalFiles()
            ));

            // return result
            PrintUtil.info("Incremental backup finished.");
            feedback = new IncrementalBackupFeedback(true, copyResult);
        } catch (IOException e) {
            e.printStackTrace(); // at least we should print it out if we discard the exception... Better than doing nothing.
            feedback = new IncrementalBackupFeedback(false, null);
        }

        if (!feedback.isSuccess()) {
            LOGGER.severe("Failed to backup.");
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
        SavedIncrementalBackup info = IncBackupInfoSerializer.fromFile(
                new File(backupIndexFileSaveDirectory, backupIndexFileName)
        );

        PrintUtil.info("Backup Info: " + info);

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
        int restoreObjectCount = storageManager.restoreObjectCollection(info.getObjectCollection(), levelPathFile);

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
