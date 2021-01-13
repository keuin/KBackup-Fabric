package com.keuin.kbackupfabric.operation;

import com.keuin.kbackupfabric.operation.abstracts.InvokableBlockingOperation;
import com.keuin.kbackupfabric.operation.backup.method.ConfiguredBackupMethod;
import com.keuin.kbackupfabric.operation.backup.method.ConfiguredPrimitiveBackupMethod;
import com.keuin.kbackupfabric.util.PrintUtil;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;

import java.io.IOException;

public class RestoreOperation extends InvokableBlockingOperation {

    //private static final Logger LOGGER = LogManager.getLogger();
    private final String backupFileName;
    private final Thread serverThread;
    private final CommandContext<ServerCommandSource> context;
    private final MinecraftServer server;
    private final ConfiguredBackupMethod configuredBackupMethod;

    public RestoreOperation(CommandContext<ServerCommandSource> context, String backupSavePath, String levelPath, String backupFileName) {
        server = context.getSource().getMinecraftServer();
        this.backupFileName = backupFileName;
        this.serverThread = server.getThread();
        this.context = context;
        this.configuredBackupMethod = new ConfiguredPrimitiveBackupMethod(backupFileName, levelPath, backupSavePath);
    }

    @Override
    protected boolean blockingContext() {
        // do restore to backupName
        PrintUtil.broadcast(String.format("Restoring to backup %s ...", backupFileName));

        PrintUtil.debug("Backup file name: " + backupFileName);

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
        return String.format("restoration from %s", backupFileName);
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
                if (configuredBackupMethod.restore()) {
                    //ServerRestartUtil.forkAndRestart();
                    System.exit(111);
                } else {
                    PrintUtil.error("Failed to restore! server will not restart automatically.");
                }

            } catch (SecurityException e) {
                PrintUtil.error("An exception occurred while restoring: " + e.getMessage());
            } catch (IOException e) {
                PrintUtil.error(e.toString());
                PrintUtil.error("Failed to restore.");
            }
            System.exit(0); // all failed restoration will eventually go here
        }
    }
}
