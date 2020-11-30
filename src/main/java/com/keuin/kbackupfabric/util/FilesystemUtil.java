package com.keuin.kbackupfabric.util;

import java.io.File;

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
        try{
            File backupZipFile = new File(filePath);
            fileSize = backupZipFile.length();
        } catch (SecurityException ignored){
        }
        return fileSize;
    }

}
