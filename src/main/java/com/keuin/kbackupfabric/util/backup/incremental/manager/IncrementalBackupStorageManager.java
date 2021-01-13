package com.keuin.kbackupfabric.util.backup.incremental.manager;

import com.keuin.kbackupfabric.util.PrintUtil;
import com.keuin.kbackupfabric.util.backup.incremental.ObjectCollection;
import com.keuin.kbackupfabric.util.backup.incremental.ObjectElement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

import static org.apache.commons.io.FileUtils.forceDelete;

public class IncrementalBackupStorageManager {

    private final Path backupStorageBase;

    public IncrementalBackupStorageManager(Path backupStorageBase) {
        this.backupStorageBase = backupStorageBase;
    }

    /**
     * Add a object collection to storage base.
     * @param collection the collection.
     * @return objects copied to the base.
     * @throws IOException I/O Error.
     */
    public int addObjectCollection(ObjectCollection collection, File collectionBasePath) throws IOException {
        if (!backupStorageBase.toFile().isDirectory()) {
            if (!backupStorageBase.toFile().mkdirs())
                throw new IOException("Backup storage base directory does not exist, and failed to create it.");
        }
        Objects.requireNonNull(collection);
        Objects.requireNonNull(collectionBasePath);

        int copyCount = 0;

        // copy sub files
        for (Map.Entry<String, ObjectElement> entry : collection.getElementMap().entrySet()) {
            File copyDestination = new File(backupStorageBase.toFile(), entry.getValue().getIdentifier().getIdentification());
            if (!baseContainsObject(entry.getValue())) {
                // element does not exist. copy.
                Files.copy(Paths.get(collectionBasePath.getAbsolutePath(), entry.getKey()), copyDestination.toPath());
                ++copyCount;
            }
        }

        //copy sub dirs recursively
        for (Map.Entry<String, ObjectCollection> entry : collection.getSubCollectionMap().entrySet()) {
            File newBase = new File(collectionBasePath, entry.getKey());
            copyCount += addObjectCollection(entry.getValue(), newBase);
        }

        return copyCount;
    }

    /**
     * Restore an object collection from the storage base. i.e., restore the save from backup storage.
     * @param collection the collection to be restored.
     * @param collectionBasePath save path of the collection.
     * @return objects restored from the base.
     * @throws IOException I/O Error.
     */
    public int restoreObjectCollection(ObjectCollection collection, File collectionBasePath) throws IOException {
        Objects.requireNonNull(collection);
        Objects.requireNonNull(collectionBasePath);

        int copyCount = 0;

        // touch directory
        if (!collectionBasePath.exists()) {
            int retryCounter = 0;
            boolean success = false;
            while (retryCounter++ < 5) {
                if (collectionBasePath.mkdirs()) {
                    success = true;
                    break;
                }
            }
            if (!success) {
                throw new IOException("Failed to create directory " + collectionBasePath.getAbsolutePath());
            }
        }

        // copy sub files
        for (Map.Entry<String, ObjectElement> entry : collection.getElementMap().entrySet()) {
            File copySource = new File(backupStorageBase.toFile(), entry.getValue().getIdentifier().getIdentification());
            File copyTarget = new File(collectionBasePath.getAbsolutePath(), entry.getKey());

            if (!baseContainsObject(entry.getValue())) {
                throw new IOException(String.format("File %s does not exist in the base.", copySource.getName()));
            }
            if (copyTarget.exists()) {
                boolean successDeleting = false;
                for (int i = 0; i < 5; ++i) {
                    try {
                        forceDelete(copyTarget);
                        successDeleting = true;
                        break;
                    } catch (FileNotFoundException ignored) {
                        break;
                    } catch (IOException e) {
                        PrintUtil.error(String.format("Failed to delete file %s, retry.", copyTarget.getName()));
                    }
                }
                if (!successDeleting) {
                    String msg = String.format("Failed to delete file %s.", copyTarget.getName());
                    PrintUtil.error(msg);
                    throw new IOException(msg);
                }
            }

            Files.copy(copySource.toPath(), copyTarget.toPath());
            ++copyCount;
        }

        //copy sub dirs recursively
        for (Map.Entry<String, ObjectCollection> entry : collection.getSubCollectionMap().entrySet()) {
            File newBase = new File(collectionBasePath, entry.getKey());
            copyCount += restoreObjectCollection(entry.getValue(), newBase);
        }

        return copyCount;
    }

    /**
     * Check if the backup base contains given element.
     * @param objectElement the element.
     * @return true or false.
     */
    private boolean baseContainsObject(ObjectElement objectElement) {
        // This may be extended to use more variants of hash functions and combinations of other attributes (such as file size)
        return (new File(backupStorageBase.toFile(), objectElement.getIdentifier().getIdentification())).exists();
    }

}
