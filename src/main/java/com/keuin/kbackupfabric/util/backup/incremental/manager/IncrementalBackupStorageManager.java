package com.keuin.kbackupfabric.util.backup.incremental.manager;

import com.keuin.kbackupfabric.util.backup.incremental.ObjectCollection;
import com.keuin.kbackupfabric.util.backup.incremental.ObjectElement;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

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

        // copy sub files
        for (Map.Entry<String, ObjectElement> entry : collection.getElementMap().entrySet()) {
            File copySource = new File(backupStorageBase.toFile(), entry.getValue().getIdentifier().getIdentification());
            if (!baseContainsObject(entry.getValue())) {
                throw new IOException(String.format("File %s does not exist in the base.", copySource.getName()));
            }
            Files.copy(copySource.toPath(), Paths.get(collectionBasePath.getAbsolutePath(), entry.getKey()));
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
