package com.keuin.kbackupfabric.operation;

import com.keuin.kbackupfabric.operation.abstracts.InvokableAsyncBlockingOperation;
import com.keuin.kbackupfabric.util.BackupNameSuggestionProvider;
import com.keuin.kbackupfabric.util.PrintUtil;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;

import java.io.File;
import java.io.IOException;

import static com.keuin.kbackupfabric.util.BackupFilesystemUtil.getBackupFileName;
import static com.keuin.kbackupfabric.util.BackupFilesystemUtil.getBackupSaveDirectory;
import static com.keuin.kbackupfabric.util.PrintUtil.msgErr;
import static com.keuin.kbackupfabric.util.PrintUtil.msgInfo;
import static org.apache.commons.io.FileUtils.forceDelete;

public class DeleteOperation extends InvokableAsyncBlockingOperation {

    //private static final Logger LOGGER = LogManager.getLogger();
    private final String backupName;
    private final CommandContext<ServerCommandSource> context;

    public DeleteOperation(CommandContext<ServerCommandSource> context, String backupName) {
        super("BackupDeletingWorker");
        this.backupName = backupName;
        this.context = context;
    }

    @Override
    public String toString() {
        return String.format("deletion of %s", backupName);
    }

    @Override
    protected void async() {
        delete();
        BackupNameSuggestionProvider.updateCandidateList();
    }

    private void delete() {
        MinecraftServer server = context.getSource().getMinecraftServer();
        String backupFileName = getBackupFileName(backupName);
        PrintUtil.info("Deleting backup " + backupName);
        File backupFile = new File(getBackupSaveDirectory(server), backupFileName);
        int tryCounter = 0;
        do {
            if (tryCounter == 5) {
                String msg = "Failed to delete file " + backupFileName;
                PrintUtil.error(msg);
                msgErr(context, msg);
                return;
            }
            try {
                if (!backupFile.delete())
                    forceDelete(backupFile);
            } catch (SecurityException | NullPointerException | IOException ignored) {
            }
            ++tryCounter;
        } while (backupFile.exists());
        PrintUtil.info("Deleted backup " + backupName);
        msgInfo(context, "Deleted backup " + backupName);
    }
}
