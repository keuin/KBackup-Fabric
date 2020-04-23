package com.keuin.kbackupfabric.worker;

import com.keuin.kbackupfabric.data.BackupMetadata;
import com.keuin.kbackupfabric.util.PrintUtil;
import com.keuin.kbackupfabric.util.ZipUtil;
import com.keuin.kbackupfabric.util.ZipUtilException;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.keuin.kbackupfabric.util.BackupFilesystemUtil.*;
import static com.keuin.kbackupfabric.util.PrintUtil.msgInfo;

/**
 * The backup worker
 * To invoke this worker, simply call invoke() method.
 */
public final class BackupWorker implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger();
    private final CommandContext<ServerCommandSource> context;
    private final String backupName;
    private final Map<World, Boolean> oldWorldsSavingDisabled;
    private final BackupMetadata backupMetadata;
    private final long startTime;

    private BackupWorker(CommandContext<ServerCommandSource> context, String backupName, Map<World, Boolean> oldWorldsSavingDisabled, BackupMetadata backupMetadata, long startTime) {
        this.context = context;
        this.backupName = backupName;
        this.oldWorldsSavingDisabled = oldWorldsSavingDisabled;
        this.backupMetadata = backupMetadata;
        this.startTime = startTime;
    }

    public static void invoke(CommandContext<ServerCommandSource> context, String backupName, BackupMetadata backupMetadata) {
        //// Save world, save old autosave configs

        PrintUtil.msgInfo(context, String.format("Making backup %s, please wait ...", backupName), true);
        Map<World, Boolean> oldWorldsSavingDisabled = new HashMap<>(); // old switch stat

        // Get server
        MinecraftServer server = context.getSource().getMinecraftServer();

        // Save old autosave switch stat temporally
        server.getWorlds().forEach(world -> {
            oldWorldsSavingDisabled.put(world, world.savingDisabled);
            world.savingDisabled = true;
        });

        // Force to save all player data and worlds
        LOGGER.debug("Saving players ...");
        server.getPlayerManager().saveAllPlayerData();
        LOGGER.debug("Saving worlds ...");
        server.save(true, true, true);

        // Start threaded worker
        BackupWorker worker = new BackupWorker(context, backupName, oldWorldsSavingDisabled, backupMetadata, System.currentTimeMillis());
        Thread workerThread = new Thread(worker, "BackupWorker");
        workerThread.start();
    }

    @Override
    public void run() {
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
            LOGGER.debug(String.format("zip(srcPath=%s, destPath=%s)", levelPath, backupSaveDirectoryFile.toString()));
            ZipUtil.makeBackupZip(levelPath, backupSaveDirectoryFile.toString(), backupFileName, backupMetadata);
            File backupZipFile = new File(backupSaveDirectoryFile, backupFileName);

            // Restore old autosave switch stat
            server.getWorlds().forEach(world -> world.savingDisabled = oldWorldsSavingDisabled.getOrDefault(world, true));

            // Print finish message: time elapsed and file size
            long timeEscapedMillis = System.currentTimeMillis() - startTime;
            msgInfo(context, String.format("Backup finished. (%.2fs)", timeEscapedMillis / 1000.0), true);
            try {
                double fileSize = backupZipFile.length() * 1.0 / 1024 / 1024;
                if (fileSize > 1000)
                    msgInfo(context, String.format("File size: %.2fGB", fileSize / 1024));
                else
                    msgInfo(context, String.format("File size: %.2fMB", fileSize));
            } catch (SecurityException ignored) {
            }

        } catch (SecurityException e) {
            msgInfo(context, String.format("Failed to create backup saving directory: %s. Failed to backup.", backupSaveDirectory));
        } catch (IOException | ZipUtilException e) {
            msgInfo(context, "Failed to make zip: " + e.getMessage());
        }
    }
}
