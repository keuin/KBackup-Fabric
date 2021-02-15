package com.keuin.kbackupfabric.backup.incremental.manager;

import com.keuin.kbackupfabric.backup.incremental.ObjectCollection2;
import com.keuin.kbackupfabric.backup.incremental.ObjectCollectionFactory;
import com.keuin.kbackupfabric.backup.incremental.ObjectCollectionIterator;
import com.keuin.kbackupfabric.backup.incremental.identifier.ObjectIdentifier;
import com.keuin.kbackupfabric.backup.incremental.identifier.Sha256Identifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IncrementalBackupStorageManagerTest {

    private static final String testRoot = "testfile/IncrementalBackupStorageManagerTest";
    private static final String srcRoot = testRoot + "/src";
    private static final String destRoot = testRoot + "/dest";

    private final Map<String, ObjectIdentifier> files = new HashMap<>();

    private static void deleteDirectoryTree(Path directory) throws IOException {
        if (!directory.toFile().exists())
            return;
        if (!directory.toFile().isDirectory() && !directory.toFile().delete())
            throw new IOException("failed to delete file " + directory);

        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static ObjectIdentifier addFile(String fileName) throws IOException {
        final File root = new File(srcRoot);
        final File dest = new File(root, fileName);
        final Random rnd = new Random();
        if (dest.exists()) {
            deleteDirectoryTree(dest.toPath());
        }
        try (FileOutputStream fos = new FileOutputStream(dest)) {
            fos.write(rnd.nextInt());
        }
        return Sha256Identifier.fromFile(dest);
    }

    private static void randomlyDeleteFiles(int count) throws IOException {
        if (count == 0)
            return;
        if (count < 0)
            throw new IllegalArgumentException("negative count");
        int[] c = new int[]{0};
        Random rnd = new Random();
        Files.walk(new File(srcRoot).toPath(), 1).map(p -> rnd.nextBoolean() ? p : null)
                .filter(Objects::nonNull).forEach(p -> {
            if (c[0]++ != count) {
                if (!p.toFile().delete() && p.toFile().isFile())
                    throw new RuntimeException("Failed to delete file " + p);
            }
        });
    }

    private static String getRandomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    @Before
    public void initialize() throws IOException {
        // init directory structure
        final File rootFile = new File(testRoot);
        deleteDirectoryTree(rootFile.toPath());
        assertTrue(rootFile.mkdir());
        assertTrue(new File(srcRoot).mkdir());
        assertTrue(new File(destRoot).mkdir());
        assertTrue(rootFile.isDirectory());
        assertTrue(new File(srcRoot).isDirectory());
        assertTrue(new File(destRoot).isDirectory());

        // add some random files
        files.clear();
        for (int i = 0; i < 100; i++) {
            String fileName = getRandomString(20);
            files.put(fileName, addFile(fileName));
        }
    }

    @After
    public void clean() throws IOException {
        deleteDirectoryTree(new File(testRoot).toPath());
    }

    @Test
    public void testDeleteAll() throws IOException {
        ObjectCollection2 col = new ObjectCollectionFactory<>(Sha256Identifier::fromFile,
                1, 1)
                .fromDirectory(new File(srcRoot));
        IncrementalBackupStorageManager manager =
                new IncrementalBackupStorageManager(new File(destRoot).toPath());
        manager.addObjectCollection(col, new File(srcRoot));
        assertTrue(manager.contains(col));
        manager.deleteObjectCollection(col);
        new ObjectCollectionIterator(col).forEachRemaining(
                ele -> assertFalse(manager.contains(ele.getIdentifier())));
    }

    @Test
    public void testDeleteNone() throws IOException {
        ObjectCollection2 col = new ObjectCollectionFactory<>(Sha256Identifier::fromFile,
                1, 1)
                .fromDirectory(new File(srcRoot));
        IncrementalBackupStorageManager manager =
                new IncrementalBackupStorageManager(new File(destRoot).toPath());
        manager.addObjectCollection(col, new File(srcRoot));
        assertTrue(manager.contains(col));
        manager.deleteObjectCollection(col, Collections.singleton(col));
        new ObjectCollectionIterator(col).forEachRemaining(
                ele -> assertTrue(manager.contains(ele.getIdentifier())));
    }

    @Test
    public void testDeleteSub() throws IOException {
        // create partial collection
        ObjectCollection2 col = new ObjectCollectionFactory<>(Sha256Identifier::fromFile,
                1, 1)
                .fromDirectory(new File(srcRoot));
        IncrementalBackupStorageManager manager =
                new IncrementalBackupStorageManager(new File(destRoot).toPath());
        manager.addObjectCollection(col, new File(srcRoot));
        assertTrue(manager.contains(col));
        manager.deleteObjectCollection(col);
        new ObjectCollectionIterator(col).forEachRemaining(
                ele -> assertFalse(manager.contains(ele.getIdentifier())));

        // expand
        randomlyDeleteFiles(25);
        for (int i = 0; i < 50; i++) {
            String fileName = getRandomString(20);
            files.put(fileName, addFile(fileName));
        }
        ObjectCollection2 col2 = new ObjectCollectionFactory<>(Sha256Identifier::fromFile,
                1, 1)
                .fromDirectory(new File(srcRoot));
        manager.addObjectCollection(col2, new File(srcRoot));
        manager.deleteObjectCollection(col, Collections.singleton(col2));
        assertTrue(manager.contains(col2));
    }

    @Test
    public void testDeleteSuper() throws IOException {
        // create partial collection
        ObjectCollection2 col = new ObjectCollectionFactory<>(Sha256Identifier::fromFile,
                1, 1)
                .fromDirectory(new File(srcRoot));
        IncrementalBackupStorageManager manager =
                new IncrementalBackupStorageManager(new File(destRoot).toPath());
        manager.addObjectCollection(col, new File(srcRoot));
        assertTrue(manager.contains(col));
        manager.deleteObjectCollection(col);
        new ObjectCollectionIterator(col).forEachRemaining(
                ele -> assertFalse(manager.contains(ele.getIdentifier())));

        // expand
        for (int i = 0; i < 50; i++) {
            String fileName = getRandomString(20);
            files.put(fileName, addFile(fileName));
        }
        ObjectCollection2 col2 = new ObjectCollectionFactory<>(Sha256Identifier::fromFile,
                1, 1)
                .fromDirectory(new File(srcRoot));
        manager.addObjectCollection(col2, new File(srcRoot));
        manager.deleteObjectCollection(col2, Collections.singleton(col));
        assertTrue(manager.contains(col));
    }
}