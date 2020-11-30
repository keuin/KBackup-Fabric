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

                int cnt = 5;
                do {
                    PrintUtil.info(String.format("Wait %d seconds ...", cnt));
                    try{
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }
                }while(--cnt > 0);

                ////////////////////

                //ServerRestartUtil.forkAndRestart();
                System.exit(111);

            } catch (SecurityException e) {
                PrintUtil.error("An exception occurred while restoring: " + e.getMessage());
            }
        }
    }
}
