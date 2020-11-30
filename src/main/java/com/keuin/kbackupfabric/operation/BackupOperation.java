package com.keuin.kbackupfabric.operation;

import com.keuin.kbackupfabric.exception.ZipUtilException;
import com.keuin.kbackupfabric.metadata.BackupMetadata;
import com.keuin.kbackupfabric.operation.abstracts.InvokableAsyncBlockingOperation;
import com.keuin.kbackupfabric.operation.backup.BackupMethod;
import com.keuin.kbackupfabric.util.PrintUtil;
import com.keuin.kbackupfabric.util.backup.builder.BackupFileNameBuilder;
import com.keuin.kbackupfabric.util.backup.formatter.BackupFileNameFormatter;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.World;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.keuin.kbackupfabric.util.backup.BackupFilesystemUtil.*;
import static com.keuin.kbackupfabric.util.PrintUtil.msgInfo;

public class BackupOperation extends InvokableAsyncBlockingOperation {

    private final CommandContext<ServerCommandSource> context;
    private final String backupName;
    private final Map<World, Boolean> oldWorldsSavingDisabled = new HashMap<>();
    private final BackupMethod backupMethod;
    private long startTime;


    public BackupOperation(CommandContext<ServerCommandSource> context, String backupName, BackupMethod backupMethod) {
        super("BackupWorker");
        this.context = context;
        this.backupName = backupName;
        this.backupMethod = backupMethod;
    }

    @Override
    protected void async() {
        String backupSaveDirectory = "";
        MinecraftServer server = context.getSource().getMinecraftServer();
        try {
            //// Do our main backup logic

            // Create backup saving directory
            File backupSaveDirectoryFile = getBackupSaveDirectory(server);
            backupSaveDirectory = backupSaveDirectoryFile.getName();
            if (!backupSaveDirectoryFile.isDirectory() && !backupSaveDirectoryFile.mkdir()) {
                msgInfo(context, String.format("Failed to create backup saving directory: %s. Failed to backup.", backupSaveDirectory));
                return;
            }

            // Make zip
            String levelPath = getLevelPath(server);
            String backupFileName = getBackupFileName(backupName);

            BackupMethod.BackupResult result = backupMethod.backup(backupName,levelPath,backupSaveDirectory);
            if(result.isSuccess()) {
                // Restore old autosave switch stat
                server.getWorlds().forEach(world -> world.savingDisabled = oldWorldsSavingDisabled.getOrDefault(world, true));

                // Print finish message: time elapsed and file size
                long timeElapsedMillis = System.currentTimeMillis() - startTime;
                String msgText = String.format("Backup finished. Time elapsed: %.2fs.", timeElapsedMillis / 1000.0);
                File backupZipFile = new File(backupSaveDirectory, backupFileName);
                msgText += String.format(" File size: %s.", humanFileSize(result.getBackupSizeBytes()));
                PrintUtil.msgInfo(context, msgText, true);
            } else {
                // failed
                PrintUtil.msgErr(context, "Backup operation failed. No further information.");
            }
        } catch (SecurityException e) {
            msgInfo(context, String.format("Failed to create backup saving directory: %s. Failed to backup.", backupSaveDirectory));
        } catch (IOException e) {
            msgInfo(context, "Failed to make zip: " + e.getMessage());
        }
    }

    @Override
    protected boolean sync() {
        //// Save world, save old autosave configs

        PrintUtil.broadcast(String.format("Making backup %s, please wait ...", backupName));

        // Get server
        MinecraftServer server = context.getSource().getMinecraftServer();

        // Save old autosave switch stat temporally
        oldWorldsSavingDisabled.clear();
        server.getWorlds().forEach(world -> {
            oldWorldsSavingDisabled.put(world, world.savingDisabled);
            world.savingDisabled = true;
        });

        // Force to save all player data and worlds
        PrintUtil.msgInfo(context, "Saving players ...");
        server.getPlayerManager().saveAllPlayerData();
        PrintUtil.msgInfo(context, "Saving worlds ...");
        server.save(true, true, true);

        // Log start time
        startTime = System.currentTimeMillis();
        return true;
    }
}
