package com.keuin.kbackupfabric;

import com.keuin.kbackupfabric.util.ZipUtil;
import com.keuin.kbackupfabric.util.ZipUtilException;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class KBCommandHandler {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final int SUCCESS = 1;
    private static final int FAILED = -1;
    private static final boolean printDebugMessages = true;
    private static final boolean printErrorMessages = true;
    private static final String backupSaveDirectoryName = "backups";
    private static final String backupFileNamePrefix = "kbackup-";

    /**
     * Print the help menu.
     *
     * @param context the context.
     * @return stat code.
     */
    public static int help(CommandContext<ServerCommandSource> context) {
        message(context, "KBackup Manual");
        message(context, "/kb | /kb help            Print help menu.");
        message(context, "/kb list                  Show all backups.");
        message(context, "/kb backup [backup_name]  Backup world, nether, end to backup_name. By default, the name is current system time.");
        message(context, "/kb restore <backup_name> Delete current three worlds, restore the older version from given backup. By default, this command is identical with /kb list.");
        return SUCCESS;
    }

    public static int list(CommandContext<ServerCommandSource> context) {
        message(context, "Available backups: (file is not checked, manipulation may affect this plugin)");
        MinecraftServer server = context.getSource().getMinecraftServer();
        File[] files = getBackupSaveDirectory(server).listFiles(
                (dir, name) -> dir.isDirectory() && name.toLowerCase().endsWith(".zip") && name.toLowerCase().startsWith(backupFileNamePrefix)
        );
        if (files != null) {
            for (File file : files) {
                message(context, file.getName());
            }
        }
        return SUCCESS;
    }

    /**
     * Backup with context parameter backupName.
     *
     * @param context the context.
     * @return stat code.
     */
    public static int backup(CommandContext<ServerCommandSource> context) {
        //KBMain.backup("name")
        return doBackup(context, StringArgumentType.getString(context, "backupName"));
    }

    /**
     * Backup with default name.
     *
     * @param context the context.
     * @return stat code.
     */
    public static int backupWithDefaultName(CommandContext<ServerCommandSource> context) {
        //KBMain.backup("name")
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        String timeString = LocalDateTime.now().format(formatter);
        return doBackup(context, timeString);
    }

    private static int doBackup(CommandContext<ServerCommandSource> context, String backupName) {
        String destPathFolderName = "";
        try {
            message(context, String.format("Making backup %s ...", backupName), true);
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

            //// Do our main backup logic

            // Get the level folder
            File sourcePathFile = new File(server.getRunDirectory(), server.getLevelName());

            // Create backup saving directory
            File destPathFile = getBackupSaveDirectory(server);
            destPathFolderName = destPathFile.getName();
            if (!destPathFile.mkdir() && !destPathFile.isDirectory()) {
                message(context, String.format("Failed to create backup saving directory: %s. Failed to backup.", destPathFolderName));
                return FAILED;
            }

            // Make zip
            debug(String.format("zip(srcPath=%s, destPath=%s)", sourcePathFile.getAbsolutePath(), destPathFile.toString()));
            ZipUtil.zip(sourcePathFile.getAbsolutePath(), destPathFile.toString(), backupFileNamePrefix + backupName + ".zip");


            // Restore old autosave switch stat
            server.getWorlds().forEach(world -> world.savingDisabled = oldWorldsSavingDisabled.getOrDefault(world, true));

            message(context, "Done.", true);
            return SUCCESS;
        } catch (SecurityException e) {
            message(context, String.format("Failed to create backup saving directory: %s. Failed to backup.", destPathFolderName));
            return FAILED;
        } catch (IOException | ZipUtilException e) {
            message(context, "Failed to make zip: " + e.getMessage());
            return FAILED;
        }
    }

    private static File getBackupSaveDirectory(MinecraftServer server) {
        return new File(server.getRunDirectory(), backupSaveDirectoryName);
    }

    /**
     * Restore with context parameter backupName.
     *
     * @param context the context.
     * @return stat code.
     */
    public static int restore(CommandContext<ServerCommandSource> context) {
        //KBMain.restore("name")
        String backupName = StringArgumentType.getString(context, "backupName");
        message(context, String.format("Restoring worlds to %s ...", backupName), true);
        // do restore to backupName

        message(context, "Done.", true);
        return SUCCESS;
    }

    private static CommandContext<ServerCommandSource> message(CommandContext<ServerCommandSource> context, String messageText) {
        return message(context, messageText, false);
    }

    private static CommandContext<ServerCommandSource> message(CommandContext<ServerCommandSource> context, String messageText, boolean broadcastToOps) {
        context.getSource().sendFeedback(new LiteralText("[KBackup] " + messageText), broadcastToOps);
        return context;
    }

    static boolean opPermissionValidator(ServerCommandSource commandSource) {
        return commandSource.hasPermissionLevel(4);
    }

    private static void debug(String debugMessage) {
        if (printDebugMessages) {
            System.out.println(String.format("[DEBUG] [KBackup] %s", debugMessage));
            LOGGER.debug(debugMessage);
        }
    }

    private static void error(String errorMessage) {
        if (printErrorMessages) {
            System.out.println(String.format("[ERROR] [KBackup] %s", errorMessage));
            LOGGER.error(errorMessage);
        }
    }

}
