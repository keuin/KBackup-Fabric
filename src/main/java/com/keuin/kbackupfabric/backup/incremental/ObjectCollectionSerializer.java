package com.keuin.kbackupfabric.backup.incremental;

import java.io.*;
import java.util.Objects;

/**
 * Serialize and deserialize ObjectCollection from/to the disk file.
 */
public class ObjectCollectionSerializer {
    public static ObjectCollection fromFile(File file) throws IOException {
        Objects.requireNonNull(file);
        ObjectCollection collection;
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            try (ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
                collection = (ObjectCollection) objectInputStream.readObject();
            } catch (ClassNotFoundException ignored) {
                // this should not happen
                return null;
            }
        }
        return collection;
    }

    public static void toFile(ObjectCollection collection, File file) throws IOException {
        Objects.requireNonNull(collection);
        Objects.requireNonNull(file);
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
                objectOutputStream.writeObject(collection);
            }
        }
    }
}
