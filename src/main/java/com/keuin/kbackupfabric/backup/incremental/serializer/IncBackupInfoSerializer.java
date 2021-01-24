package com.keuin.kbackupfabric.backup.incremental.serializer;

import com.keuin.kbackupfabric.backup.incremental.ObjectCollection2;
import com.keuin.kbackupfabric.backup.incremental.ObjectCollectionConverter;
import com.keuin.kbackupfabric.backup.name.BackupFileNameEncoder;
import com.keuin.kbackupfabric.backup.name.IncrementalBackupFileNameEncoder;
import com.keuin.kbackupfabric.util.backup.incremental.ObjectCollection;

import java.io.*;
import java.util.Objects;

public class IncBackupInfoSerializer {
    /**
     * Load incremental backup index file into object, no matter what version it is.
     *
     * @param file a valid incremental backup file. (with a valid file name)
     * @return the object. Not null.
     * @throws IOException when failed due to an I/O error.
     */
    public static SavedIncrementalBackup fromFile(File file) throws IOException {
        Objects.requireNonNull(file);
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            try (ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
                Object o = objectInputStream.readObject();
                if (o instanceof SavedIncrementalBackup) {
                    return (SavedIncrementalBackup) o;
                } else if (o instanceof ObjectCollection) {
                    // backward compatibility with old-style (v0) incremental backup
                    BackupFileNameEncoder.BackupBasicInformation info = IncrementalBackupFileNameEncoder.INSTANCE.decode(file.getName());
                    if (info == null)
                        throw new IOException("Invalid backup file name.");
                    return new SavedIncBackupV0(ObjectCollectionConverter.convert((ObjectCollection) o), info);
                } else if (o instanceof ObjectCollection2) {
                    // compatible with 1.4.6 implementation
                    BackupFileNameEncoder.BackupBasicInformation info = IncrementalBackupFileNameEncoder.INSTANCE.decode(file.getName());
                    if (info == null)
                        throw new IOException("Invalid backup file name.");
                    return new SavedIncBackupV0((ObjectCollection2) o, info);
                } else {
                    throw new RuntimeException("Unrecognized backup file format: unknown class " + o.getClass().getCanonicalName());
                }
            } catch (ClassNotFoundException e) {
                // this should not happen
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            System.err.println("Failed to deserialize file " + file.getName());
            throw e;
        }
    }

    /**
     * Save incremental backup index and metadata into file.
     *
     * @param file   the file.
     * @param backup the backup.
     * @throws IOException when failed due to an I/O error.
     */
    public static void toFile(File file, SavedIncrementalBackup backup) throws IOException {
        Objects.requireNonNull(file);
        Objects.requireNonNull(backup);
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
                objectOutputStream.writeObject(backup);
            }
        }
    }
}
