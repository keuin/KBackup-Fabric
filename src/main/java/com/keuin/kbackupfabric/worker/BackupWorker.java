package com.keuin.kbackupfabric.worker;

import com.keuin.kbackupfabric.util.ZipUtil;
import com.keuin.kbackupfabric.util.ZipUtilException;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.World;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.keuin.kbackupfabric.util.BackupFilesystemUtil.*;
import static com.keuin.kbackupfabric.util.PrintUtil.debug;
import static com.keuin.kbackupfabric.util.PrintUtil.message;

/**
 * The backup worker
 * To invoke this worker, simply call invoke() method.
 */
public final class BackupWorker implements Runnable {
    private final CommandContext<ServerCommandSource> context;
    private final String backupName;
    private final Map<World, Boolean> oldWorldsSavingDisabled;


    private BackupWorker(CommandContext<ServerCommandSource> context, String backupName, Map<World, Boolean> oldWorldsSavingDisabled) {
        this.context = context;
        this.backupName = backupName;
        this.oldWorldsSavingDisabled = oldWorldsSavingDisabled;
    }

    public static void invoke(CommandContext<ServerCommandSource> context, String backupName) {
        //// Save world, save old autosave configs

        message(context, String.format("Making backup %s, please wait ...", backupName), true);
        Map<World, Boolean> oldWorldsSavingDisabled = new HashMap<>(); // old switch stat

        // Get server
        MinecraftServer server = context.getSource().getMinecraftServer();

        // Save old autosave switch stat temporally
        server.getWorlds().forEach(world -> {
            oldWorldsSavingDisabled.put(world, world.savingDisabled);
            world.savingDisabled = true;
        });

        // Force to save all player data and worlds
        debug("Saving players ...");
        server.getPlayerManager().saveAllPlayerData();
        debug("Saving worlds ...");
        server.save(true, true, true);

        // Start threaded worker
        BackupWorker worker = new BackupWorker(context, backupName, oldWorldsSavingDisabled);
        Thread workerThread = new Thread(worker, "BackupWorker");
        workerThread.start();
    }

    @Override
    public void run() {
        String destPathFolderName = "";
        MinecraftServer server = context.getSource().getMinecraftServer();
        try {
            //// Do our main backup logic

            // Create backup saving directory
            File destPathFile = getBackupSaveDirectory(server);
            destPathFolderName = destPathFile.getName();
            if (!destPathFile.isDirectory() && !destPathFile.mkdir()) {
                message(context, String.format("Failed to create backup saving directory: %s. Failed to backup.", destPathFolderName));
                return;
            }

            // Make zip
            String levelPath = getLevelPath(server);
            debug(String.format("zip(srcPath=%s, destPath=%s)", levelPath, destPathFile.toString()));
            ZipUtil.zip(levelPath, destPathFile.toString(), getBackupFileName(backupName));

            // Restore old autosave switch stat
            server.getWorlds().forEach(world -> world.savingDisabled = oldWorldsSavingDisabled.getOrDefault(world, true));

            message(context, "Done.", true);
        } catch (SecurityException e) {
            message(context, String.format("Failed to create backup saving directory: %s. Failed to backup.", destPathFolderName));
        } catch (IOException | ZipUtilException e) {
            message(context, "Failed to make zip: " + e.getMessage());
        }
    }
}
