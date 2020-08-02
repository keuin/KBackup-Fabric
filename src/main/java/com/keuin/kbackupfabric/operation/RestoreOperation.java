package com.keuin.kbackupfabric.operation;

import com.keuin.kbackupfabric.exception.ZipUtilException;
import com.keuin.kbackupfabric.operation.abstracts.InvokableBlockingOperation;
import com.keuin.kbackupfabric.util.PrintUtil;
import com.keuin.kbackupfabric.util.ZipUtil;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;

import java.io.File;
import java.io.IOException;

import static com.keuin.kbackupfabric.util.backup.BackupFilesystemUtil.getBackupFileName;
import static com.keuin.kbackupfabric.util.backup.BackupFilesystemUtil.getBackupSaveDirectory;
import static org.apache.commons.io.FileUtils.forceDelete;

public class RestoreOperation extends InvokableBlockingOperation {

    //private static final Logger LOGGER = LogManager.getLogger();
    private final String backupName;
    private final Thread serverThread;
    private final String backupFilePath;
    private final String levelDirectory;
    private final CommandContext<ServerCommandSource> context;
    private final MinecraftServer server;

    public RestoreOperation(CommandContext<ServerCommandSource> context, String backupFilePath, String levelDirectory, String backupName) {
        server = context.getSource().getMinecraftServer();
        this.backupName = backupName;
        this.serverThread = server.getThread();
        this.backupFilePath = backupFilePath;
        this.levelDirectory = levelDirectory;
        this.context = context;
    }

    @Override
    protected boolean blockingContext() {
        // do restore to backupName
        PrintUtil.broadcast(String.format("Restoring to previous world %s ...", backupName));

        String backupFileName = getBackupFileName(backupName);
        PrintUtil.debug("Backup file name: " + backupFileName);
        File backupFile = new File(getBackupSaveDirectory(server), backupFileName);

        PrintUtil.msgInfo(context, "Server will shutdown in a few seconds, depending on world size and disk speed, the progress may take from seconds to minutes.", true);
        PrintUtil.msgInfo(context, "Please do not force the server stop, or the level would be broken.", true);
        PrintUtil.msgInfo(context, "After it shuts down, please restart the server manually.", true);
        final int WAIT_SECONDS = 10;
        for (int i = 0; i < WAIT_SECONDS; ++i) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
        PrintUtil.broadcast("Shutting down ...");
        //RestoreWorker worker = new RestoreWorker(server.getThread(), backupFilePath, levelDirectory);
        Thread workerThread = new Thread(new WorkerThread(), "RestoreWorker");
        workerThread.start();
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }
        server.stop(false);
        return true;
    }

    @Override
    public String toString() {
        return String.format("restoration from %s", backupName);
    }

    private class WorkerThread implements Runnable {

        @Override
        public void run() {
            try {
                // Wait server thread die
                PrintUtil.info("Waiting for the server thread to exit ...");
                while (serverThread.isAlive()) {
                    try {
                        serverThread.join();
                    } catch (InterruptedException ignored) {
                    }
                }

                PrintUtil.info("Wait for 5 seconds ...");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {
                }

                // Delete old level
                PrintUtil.info("Server stopped. Deleting old level ...");
                File levelDirFile = new File(levelDirectory);
                long startTime = System.currentTimeMillis();

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
                    PrintUtil.error(String.format("Cannot restore: failed to delete old level %s .", levelDirFile.getName()));
                    return;
                }

                // Decompress archive
                PrintUtil.info("Decompressing archived level ...");
                ZipUtil.unzip(backupFilePath, levelDirectory, false);
                long endTime = System.currentTimeMillis();
                PrintUtil.info(String.format("Restore complete! (%.2fs) Please restart the server manually.", (endTime - startTime) / 1000.0));
                PrintUtil.info("If you want to restart automatically after restoring, please visit the project manual at: https://github.com/keuin/KBackup-Fabric/blob/master/README.md");

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }

                //ServerRestartUtil.forkAndRestart();
                System.exit(111);

            } catch (SecurityException | IOException | ZipUtilException e) {
                PrintUtil.error("An exception occurred while restoring: " + e.getMessage());
            }
        }
    }
}
