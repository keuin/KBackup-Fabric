package com.keuin.kbackupfabric.operation.backup.method;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

import static org.apache.commons.io.FileUtils.forceDelete;
import static org.junit.Assert.*;

public class ConfiguredIncrementalBackupMethodTest {

    private final String testTempPath = "R:\\";
    private final String sourceDirectoryName = "source";
    private final String destDirectoryName = "destination";
    private final String indexFileName = "index";

    private final double directoryFactor = 0.4;
    private final double fileFactor = 0.1;
    private final int maxRandomFileSizeBytes = 1024 * 1024;
    private final Function<Integer, Integer> scaleDecayFunc = (x) -> x - 1;

    @Test
    public void iterationTest() throws IOException {
        int a = 100;
        for (int i = 0; i < a; ++i) {
            performTest(Math.min(i + 1, 10));
            System.out.println("Round " + i + " passed.");
        }
    }

    private void performTest(int scale) throws IOException {

        // init source and destination
        final Path sourcePath = Paths.get(testTempPath, sourceDirectoryName);
        final Path destPath = Paths.get(testTempPath, destDirectoryName);
        if (new File(sourcePath.toString()).exists()) {
            forceDelete(new File(sourcePath.toString()));
            if (!new File(sourcePath.toString()).mkdirs())
                fail();
        }
        if (new File(destPath.toString()).exists()) {
            forceDelete(new File(destPath.toString()));
            if (!new File(destPath.toString()).mkdirs())
                fail();
        }
        if (new File(testTempPath, indexFileName).exists()) {
            if (!new File(testTempPath, indexFileName).delete())
                fail();
        }

        // initialize src
        createRandomDirectoryTree(sourcePath.toString(), scale);

        String hash1 = calcMD5HashForDir(sourcePath.toFile(), true);

        // copy src to dest
        ConfiguredIncrementalBackupMethod method = new ConfiguredIncrementalBackupMethod(
                indexFileName,
                sourcePath.toString(),
                testTempPath,
                destPath.toString()
        );
        method.backup();

        // delete src
        forceDelete(sourcePath.toFile());
        assertFalse(sourcePath.toFile().isDirectory());

        // restore src
        if (!method.restore())
            fail();

        String hash2 = calcMD5HashForDir(sourcePath.toFile(), true);

        assertEquals(hash1, hash2);
    }

    private void createRandomDirectoryTree(String path, int scale) throws IOException {
        if (scale <= 0) {
            if (Math.random() < 0.5)
                if (!new File(path).mkdirs() && !new File(path).exists())
                    throw new IOException("Failed to create directory " + path);
            return;
        }
        if (!new File(path).isDirectory() && !new File(path).mkdirs())
            throw new IOException("Failed to create directory " + path);

        int subFileCount = (int) Math.round(Math.random() * 10 * scale * fileFactor);
        for (int i = 0; i < subFileCount; i++) {
            String subFile = null;
            while (subFile == null || new File(path, subFile).exists())
                subFile = getRandomString((int) (Math.random() * 16 + 1));
            createRandomFile(new File(path, subFile), maxRandomFileSizeBytes);
        }


        int subDirCount = (int) Math.round(Math.random() * 10 * scale * directoryFactor);
        for (int i = 0; i < subDirCount; i++) {
            String subDir = null;
            while (subDir == null || new File(path, subDir).exists())
                subDir = getRandomString((int) (Math.random() * 16 + 1));
            createRandomDirectoryTree(new File(path, subDir).getAbsolutePath(), scaleDecayFunc.apply(scale));
        }
    }

    private static void createRandomFile(File file, int maxSizeBytes) throws IOException {
        if (!file.createNewFile())
            throw new IOException("Failed to create file " + file.getAbsolutePath());
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            int fileBytes = (int) (maxSizeBytes * Math.random() + 1);
            Random random = new Random();
            final int chunkSize = 1024 * 4;
            byte[] randomChunk = new byte[chunkSize];
            for (int i = 0; i < fileBytes / chunkSize; i++) {
                random.nextBytes(randomChunk);
                fileOutputStream.write(randomChunk);
            }
            if (fileBytes % chunkSize != 0) {
                randomChunk = new byte[fileBytes % chunkSize];
                random.nextBytes(randomChunk);
                fileOutputStream.write(randomChunk);
            }
        }
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

    public String calcMD5HashForDir(File dirToHash, boolean includeHiddenFiles) {

        assert (dirToHash.isDirectory());
        Vector<FileInputStream> fileStreams = new Vector<>();

        System.out.println("Found files for hashing:");
        collectInputStreams(dirToHash, fileStreams, includeHiddenFiles);

        SequenceInputStream seqStream =
                new SequenceInputStream(fileStreams.elements());

        try {
            String md5Hash = DigestUtils.md5Hex(seqStream);
            seqStream.close();
            return md5Hash;
        } catch (IOException e) {
            throw new RuntimeException("Error reading files to hash in "
                    + dirToHash.getAbsolutePath(), e);
        }

    }

    private void collectInputStreams(File dir,
                                     List<FileInputStream> foundStreams,
                                     boolean includeHiddenFiles) {

        File[] fileList = dir.listFiles();
        Arrays.sort(fileList,               // Need in reproducible order
                new Comparator<File>() {
                    public int compare(File f1, File f2) {
                        return f1.getName().compareTo(f2.getName());
                    }
                });

        for (File f : fileList) {
            if (!includeHiddenFiles && f.getName().startsWith(".")) {
                // Skip it
            } else if (f.isDirectory()) {
                collectInputStreams(f, foundStreams, includeHiddenFiles);
            } else {
                try {
                    System.out.println("\t" + f.getAbsolutePath());
                    foundStreams.add(new FileInputStream(f));
                } catch (FileNotFoundException e) {
                    throw new AssertionError(e.getMessage()
                            + ": file should never not be found!");
                }
            }
        }

    }
}