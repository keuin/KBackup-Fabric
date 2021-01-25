package com.keuin.kbackupfabric.backup.incremental.manager;

import com.keuin.kbackupfabric.backup.incremental.ObjectCollection2;
import com.keuin.kbackupfabric.backup.incremental.ObjectCollectionIterator;
import com.keuin.kbackupfabric.backup.incremental.ObjectElement;
import com.keuin.kbackupfabric.util.FilesystemUtil;
import com.keuin.kbackupfabric.util.PrintUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static org.apache.commons.io.FileUtils.forceDelete;

/**
 * Managing the base storing all collection objects.
 */
public class IncrementalBackupStorageManager {

    private final Path backupStorageBase;
    private final Logger LOGGER = Logger.getLogger(IncrementalBackupStorageManager.class.getName());

    public IncrementalBackupStorageManager(Path backupStorageBase) {
        this.backupStorageBase = backupStorageBase;
    }

    /**
     * Add a object collection to storage base.
     *
     * @param collection the collection.
     * @return objects copied to the base.
     * @throws IOException I/O error.
     */
    public @Nullable
    IncCopyResult addObjectCollection(ObjectCollection2 collection, File collectionBasePath) throws IOException {
        if (!backupStorageBase.toFile().isDirectory()) {
            if (!backupStorageBase.toFile().mkdirs())
                throw new IOException("Backup storage base directory does not exist, and failed to create it.");
        }
        Objects.requireNonNull(collection);
        Objects.requireNonNull(collectionBasePath);

        IncCopyResult copyCount = IncCopyResult.ZERO;

        // copy sub files
        for (Map.Entry<String, ObjectElement> entry : collection.getElementMap().entrySet()) {
            File copyDestination = new File(backupStorageBase.toFile(), entry.getValue().getIdentifier().getIdentification());
            File copySourceFile = new File(collectionBasePath.getAbsolutePath(), entry.getKey());
            final long fileBytes = FilesystemUtil.getFileSizeBytes(copySourceFile.getAbsolutePath());
            if (!baseContainsObject(entry.getValue())) {
                // element does not exist. copy.
                Files.copy(copySourceFile.toPath(), copyDestination.toPath());
                copyCount = copyCount.addWith(new IncCopyResult(1, 1, fileBytes, fileBytes));
            } else {
                // element exists (file reused). Just update the stat info
                copyCount = copyCount.addWith(new IncCopyResult(1, 0, 0, fileBytes));
            }
        }

        //copy sub dirs recursively
        for (Map.Entry<String, ObjectCollection2> entry : collection.getSubCollectionMap().entrySet()) {
            File newBase = new File(collectionBasePath, entry.getKey());
            copyCount = copyCount.addWith(addObjectCollection(entry.getValue(), newBase));
        }

        return copyCount;
    }

    /**
     * Delete all files in the specific collection, from the storage base.
     *
     * @param collection the collection containing files to be deleted.
     * @return files deleted
     */
    public int deleteObjectCollection(ObjectCollection2 collection) {
        return deleteObjectCollection(collection, Collections.emptySet());
    }

    /**
     * Delete a collection from the storage base, optionally preserving files used by other backups.
     *
     * @param collection               the collection containing files to be deleted.
     * @param otherExistingCollections other collections (not to be deleted) in this base. Files exist in these collections will not be deleted.
     * @return files deleted
     */
    public int deleteObjectCollection(ObjectCollection2 collection,
                                      Iterable<ObjectCollection2> otherExistingCollections) {
        // TODO: test this
        Iterator<ObjectElement> iter = new ObjectCollectionIterator(collection);
        Set<ObjectElement> unusedElementSet = new HashSet<>();
        iter.forEachRemaining(unusedElementSet::add);
        otherExistingCollections.forEach(col -> new ObjectCollectionIterator(col).forEachRemaining(unusedElementSet::remove));
        AtomicInteger deleteCount = new AtomicInteger();
        unusedElementSet.forEach(ele -> {
            File file = new File(backupStorageBase.toFile(), ele.getIdentifier().getIdentification());
            if (file.exists()) {
                if (file.delete())
                    deleteCount.incrementAndGet();
                else
                    LOGGER.warning("Failed to delete unused file " + file.getName());
            }
        });
        return deleteCount.get();
    }

    /**
     * Restore an object collection from the storage base. i.e., restore the save from backup storage.
     *
     * @param collection         the collection to be restored.
     * @param collectionBasePath save path of the collection.
     * @return objects restored from the base.
     * @throws IOException I/O Error.
     */
    public int restoreObjectCollection(ObjectCollection2 collection, File collectionBasePath) throws IOException {
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
                throw new IOException(String.format("File %s is missing in the backup storage. Cannot restore.", copySource.getName()));
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
        for (Map.Entry<String, ObjectCollection2> entry : collection.getSubCollectionMap().entrySet()) {
            File newBase = new File(collectionBasePath, entry.getKey());
            copyCount += restoreObjectCollection(entry.getValue(), newBase);
        }

        return copyCount;
    }

    /**
     * Check if the backup base contains given element.
     *
     * @param objectElement the element.
     * @return true or false.
     */
    private boolean baseContainsObject(ObjectElement objectElement) {
        // This can be extended to use more variants of hash functions and combinations of other attributes (such as file size)
        return (new File(backupStorageBase.toFile(), objectElement.getIdentifier().getIdentification())).exists();
    }

}
