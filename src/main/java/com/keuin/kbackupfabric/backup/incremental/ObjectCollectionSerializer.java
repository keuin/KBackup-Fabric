package com.keuin.kbackupfabric.backup.incremental;

import com.keuin.kbackupfabric.backup.incremental.serializer.IncBackupInfoSerializer;
import com.keuin.kbackupfabric.backup.incremental.serializer.SavedIncrementalBackup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
        throw new RuntimeException("This method has been depreciated.");
//        Objects.requireNonNull(file);
//        ObjectCollection2 collection;
//        try (FileInputStream fileInputStream = new FileInputStream(file)) {
//            try (ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
//                collection = (ObjectCollection2) objectInputStream.readObject();
//            } catch (ClassNotFoundException ignored) {
//                // this should not happen
//                return null;
//            }
//        }
//        return collection;
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

    public static Iterable<ObjectCollection2> fromDirectory(File directory) throws IOException {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Given directory is invalid.");
        }
        List<Path> pathList = new ArrayList<>();
        Files.walk(directory.toPath(), 1).filter(p -> {
            File f = p.toFile();
            return f.isFile() && f.getName().endsWith(".kbi");
        }).forEach(pathList::add);
        List<ObjectCollection2> objectList = new ArrayList<>();
        for (Path path : pathList) {
            SavedIncrementalBackup info = IncBackupInfoSerializer.fromFile(path.toFile());
            objectList.add(info.getObjectCollection());
        }
        return Collections.unmodifiableCollection(objectList);
    }
}
