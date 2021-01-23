package com.keuin.kbackupfabric.backup.incremental;

import java.io.*;
import java.util.Objects;

/**
 * Serialize and deserialize ObjectCollection from/to the disk file.
 * Now we want to save additional metadata in incremental backups. So the serializer on pure ObjectCollection is depreciated.
 */
public class ObjectCollectionSerializer {

    /**
     * This doesn't work with the latest format. Use IncBackupInfoSerializer instead.
     */
    @Deprecated
    public static ObjectCollection2 fromFile(File file) throws IOException {
        Objects.requireNonNull(file);
        ObjectCollection2 collection;
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            try (ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
                collection = (ObjectCollection2) objectInputStream.readObject();
            } catch (ClassNotFoundException ignored) {
                // this should not happen
                return null;
            }
        }
        return collection;
    }

    /**
     * Only used for testing backward-compatibility with legacy backups.
     */
    public static void toFile(ObjectCollection2 collection, File file) throws IOException {
        Objects.requireNonNull(collection);
        Objects.requireNonNull(file);
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
                objectOutputStream.writeObject(collection);
            }
        }
    }
}
