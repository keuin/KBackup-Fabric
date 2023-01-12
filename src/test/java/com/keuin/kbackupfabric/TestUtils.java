package com.keuin.kbackupfabric;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class TestUtils {

    public static String getTempDirectory(String subDirectory) throws IOException {
        String testTempPath;
        String path = System.getenv("KB_TEMP_DIR");
        if (path == null || path.isEmpty() || !new File(path).isDirectory()) {
            path = findTempPath();
        }
        return Paths.get(path, subDirectory).toString();
    }

    private static String findTempPath() throws IOException {
        String path;
        if (System.getProperty("os.name").startsWith("Windows")) {
            // Windows
            path = System.getProperty("java.io.tmpdir");
        } else {
            // Unix
            path = System.getenv("XDG_RUNTIME_DIR");
            if (!new File(path).isDirectory()) {
                path = "/tmp";
            }
        }
        if (!new File(path).isDirectory()) {
            throw new IOException("Cannot find suitable temporary path");
        }
        return path;
    }
}
