package com.keuin.kbackupfabric.operation;

import com.keuin.kbackupfabric.util.PrintUtil;
import com.keuin.kbackupfabric.worker.RestoreWorker;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

import static com.keuin.kbackupfabric.util.BackupFilesystemUtil.*;

class RestoreOperation extends Confirmable {

    private static final Logger LOGGER = LogManager.getLogger();
    private final String backupName;
    private final CommandContext<ServerCommandSource> context;

    RestoreOperation(CommandContext<ServerCommandSource> context, String backupName) {
        this.backupName = backupName;
        this.context = context;
    }

    @Override
    public boolean confirm() {
        // do restore to backupName
        MinecraftServer server = context.getSource().getMinecraftServer();
        PrintUtil.msgInfo(context, String.format("Restoring to previous world %s ...", backupName), true);

        String backupFileName = getBackupFileName(backupName);
        LOGGER.debug("Backup file name: " + backupFileName);
        File backupFile = new File(getBackupSaveDirectory(server), backupFileName);

        PrintUtil.msgInfo(context, "Server will shutdown in a few seconds, depended on your world size and the disk speed, the restore progress may take seconds or minutes.", true);
        PrintUtil.msgInfo(context, "Please do not force the server stop, or the level would be broken.", true);
        PrintUtil.msgInfo(context, "After it shuts down, please restart the server manually.", true);
        final int WAIT_SECONDS = 10;
        for (int i = 0; i < WAIT_SECONDS; ++i) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
        PrintUtil.msgInfo(context, "Shutting down ...", true);
        RestoreWorker.invoke(server, backupFile.getPath(), getLevelPath(server));
        return true;
    }

    @Override
    public String toString() {
        return String.format("restoration from %s", backupName);
    }
}
