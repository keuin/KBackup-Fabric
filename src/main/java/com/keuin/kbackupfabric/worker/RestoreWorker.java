package com.keuin.kbackupfabric.worker;

import com.keuin.kbackupfabric.util.ZipUtil;
import com.keuin.kbackupfabric.util.ZipUtilException;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.IOException;

import static com.keuin.kbackupfabric.util.PrintUtil.*;
import static org.apache.commons.io.FileUtils.forceDelete;

/**
 * The restore worker
 * To invoke this worker, simply call invoke() method.
 */
public final class RestoreWorker implements Runnable {
    private final Thread serverThread;
    private final String backupFilePath;
    private final String levelDirectory;

    private RestoreWorker(Thread serverThread, String backupFilePath, String levelDirectory) {
        this.serverThread = serverThread;
        this.backupFilePath = backupFilePath;
        this.levelDirectory = levelDirectory;
    }

    public static void invoke(MinecraftServer server, String backupFilePath, String levelDirectory) {
        RestoreWorker worker = new RestoreWorker(server.getThread(), backupFilePath, levelDirectory);
        Thread workerThread = new Thread(worker, "RestoreWorker");
        workerThread.start();
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }
        server.stop(false);
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

            debug("Wait for 5 seconds ...");
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
                if (!levelDirFile.exists())
                    break;
                ++failedCounter;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {
                }
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
