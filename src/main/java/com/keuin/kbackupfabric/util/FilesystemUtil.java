package com.keuin.kbackupfabric.util;

import java.io.File;
import java.io.IOException;

import static org.apache.commons.io.FileUtils.forceDelete;

public class FilesystemUtil {

    /**
     * Get file sizes in bytes.
     * @param parentDirectory path to specific file.
     * @param fileName file name.
     * @return bytes. If failed, return -1.
     */
    public static long getFileSizeBytes(String parentDirectory, String fileName) {
        long fileSize = -1;
        try{
            File backupZipFile = new File(parentDirectory, fileName);
            fileSize = backupZipFile.length();
        } catch (SecurityException ignored){
        }
        return fileSize;
    }

    public static long getFileSizeBytes(String filePath) {
        long fileSize = -1;
        try {
            File backupZipFile = new File(filePath);
            fileSize = backupZipFile.length();
        } catch (SecurityException ignored) {
        }
        return fileSize;
    }

    public static boolean forceDeleteDirectory(File levelDirFile) throws IOException {
        int failedCounter = 0;
        final int MAX_RETRY_TIMES = 20;
        IOException exception = null;
        while (failedCounter < MAX_RETRY_TIMES) {
            System.gc();
            if (!levelDirFile.delete() && levelDirFile.exists()) {
                System.gc();
                try {
                    forceDelete(levelDirFile); // Try to force delete.
                } catch (IOException e) {
                    exception = e;
                }
            }
            if (!levelDirFile.exists())
                break;
            ++failedCounter;
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
        }
        if (exception != null)
            throw exception;
        if (levelDirFile.exists()) {
            PrintUtil.error(String.format("Cannot restore: failed to delete old level %s .", levelDirFile.getName()));
            return false;
        }
        return true;
    }

}
