package com.keuin.kbackupfabric.operation;

import com.keuin.kbackupfabric.exception.ZipUtilException;
import com.keuin.kbackupfabric.metadata.BackupMetadata;
import com.keuin.kbackupfabric.operation.abstracts.InvokableAsyncBlockingOperation;
import com.keuin.kbackupfabric.util.PrintUtil;
import com.keuin.kbackupfabric.util.ZipUtil;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.World;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.keuin.kbackupfabric.util.BackupFilesystemUtil.*;
import static com.keuin.kbackupfabric.util.PrintUtil.msgInfo;

public class BackupOperation extends InvokableAsyncBlockingOperation {

    private final CommandContext<ServerCommandSource> context;
    private final String backupName;
    private final Map<World, Boolean> oldWorldsSavingDisabled = new HashMap<>();
    private final BackupMetadata backupMetadata;
    private long startTime;


    public BackupOperation(CommandContext<ServerCommandSource> context, String backupName, BackupMetadata backupMetadata) {
        super("BackupWorker");
        this.context = context;
        this.backupName = backupName;
        this.backupMetadata = backupMetadata;
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
            PrintUtil.info(String.format("zip(srcPath=%s, destPath=%s)", levelPath, backupSaveDirectoryFile.toString()));
            msgInfo(context, "Compressing worlds ...");
            PrintUtil.info("Compressing level ...");
            ZipUtil.makeBackupZip(levelPath, backupSaveDirectoryFile.toString(), backupFileName, backupMetadata);
            File backupZipFile = new File(backupSaveDirectoryFile, backupFileName);

            // Restore old autosave switch stat
            server.getWorlds().forEach(world -> world.savingDisabled = oldWorldsSavingDisabled.getOrDefault(world, true));

            // Print finish message: time elapsed and file size
            long timeElapsedMillis = System.currentTimeMillis() - startTime;
            String msgText = String.format("Backup finished. Time elapsed: %.2fs.", timeElapsedMillis / 1000.0);
            try {
                msgText += String.format(" File size: %s.", humanFileSize(backupZipFile.length()));
            } catch (SecurityException ignored) {
            }
            PrintUtil.msgInfo(context, msgText, true);

        } catch (SecurityException e) {
            msgInfo(context, String.format("Failed to create backup saving directory: %s. Failed to backup.", backupSaveDirectory));
        } catch (IOException | ZipUtilException e) {
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
