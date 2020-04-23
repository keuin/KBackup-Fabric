package com.keuin.kbackupfabric.util;

import java.io.File;
import java.io.IOException;

import static com.keuin.kbackupfabric.util.IO.*;
import static org.apache.commons.io.FileUtils.forceDelete;

/**
 * This thread wait the server to be stopped (must invoke stop out of this thread),
 * then delete current level, and restore our backup.
 */
public class PostProgressRestoreThread implements Runnable {
    private final Thread serverThread;
    private final String backupFilePath;
    private final String levelDirectory;

    public PostProgressRestoreThread(Thread serverThread, String backupFilePath, String levelDirectory) {
        this.serverThread = serverThread;
        this.backupFilePath = backupFilePath;
        this.levelDirectory = levelDirectory;
    }

    @Override
    public void run() {
        try {
            // Wait server thread die
            debug("Waiting server thread stopping ...");
            while (serverThread.isAlive()) {
                try {
                    serverThread.join();
                } catch (InterruptedException ignored) {
                }
            }

            debug("Waiting ...");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {
            }
            // Delete old level
            debug("Server stopped. Deleting old level ...");
            File levelDirFile = new File(levelDirectory);

            int failedCounter = 0;
            final int MAX_RETRY_TIMES = 20;
            while (failedCounter < MAX_RETRY_TIMES) {
                System.gc();
                if (!levelDirFile.delete() && levelDirFile.exists()) {
                    System.gc();
                    forceDelete(levelDirFile); // Try to force delete.
                }
                if (levelDirFile.exists())
                    ++failedCounter;
                else
                    break;
            }
            if (levelDirFile.exists()) {
                error(String.format("Cannot restore: failed to delete old level %s .", levelDirFile.getName()));
                return;
            }

            // Decompress archive
            debug("Decompressing archived level");
            ZipUtil.unzip(backupFilePath, levelDirectory, false);
            info("Restore complete! Please restart the server manually.");
        } catch (SecurityException | IOException | ZipUtilException e) {
            error("An exception occurred while restoring: " + e.getMessage());
        }
    }
}
