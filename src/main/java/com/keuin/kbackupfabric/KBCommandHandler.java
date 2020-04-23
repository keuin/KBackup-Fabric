package com.keuin.kbackupfabric;

import com.keuin.kbackupfabric.util.PostProgressRestoreThread;
import com.keuin.kbackupfabric.util.ZipUtil;
import com.keuin.kbackupfabric.util.ZipUtilException;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.World;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static com.keuin.kbackupfabric.util.IO.debug;
import static com.keuin.kbackupfabric.util.IO.message;

public class KBCommandHandler {


    private static final int SUCCESS = 1;
    private static final int FAILED = -1;

    private static final String backupSaveDirectoryName = "backups";
    private static final String backupFileNamePrefix = "kbackup-";
    private static final HashMap<Integer, String> backupIndexNameMapper = new HashMap<>(); // index -> backupName
    private static String restoreBackupNameToBeConfirmed = null;

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
        backupIndexNameMapper.clear();
        if (files != null) {
            int i = 0;
            for (File file : files) {
                ++i;
                String backupName = getBackupName(file.getName());
                backupIndexNameMapper.put(i, backupName);
                message(context, String.format("[%d] %s, size: %.1fMB", i, backupName, file.length() * 1.0 / 1024 / 1024));
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
        String backupName = StringArgumentType.getString(context, "backupName");
        if (backupName.matches("[0-9]*")) {
            // Numeric param is not allowed
            backupName = String.format("a%s", backupName);
            message(context, String.format("Pure numeric name is not allowed. Renamed to %s", backupName));
        }
        return doBackup(context, backupName);
    }

    /**
     * Restore with context parameter backupName.
     * Simply set the pending backupName to given backupName, for the second confirmation.
     *
     * @param context the context.
     * @return stat code.
     */
    public static int restore(CommandContext<ServerCommandSource> context) {
        //KBMain.restore("name")
        String backupName = StringArgumentType.getString(context, "backupName");

        if (backupName.matches("[0-9]*")) {
            // If numeric input
            Integer index = Integer.parseInt(backupName);
            String realBackupName = backupIndexNameMapper.get(index);
            if (realBackupName == null) {
                return list(context); // Show the list and return
            }
            backupName = realBackupName; // Replace input number with real backup name.
        }

        // Update confirm pending variable
        restoreBackupNameToBeConfirmed = backupName;
        message(context, String.format("WARNING: You will LOST YOUR CURRENT WORLD COMPLETELY! It will be replaced with the backup %s . Please use /kb confirm to proceed executing.", restoreBackupNameToBeConfirmed), true);
        return SUCCESS;
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

            //// Do our main backup logic

            // Create backup saving directory
            File destPathFile = getBackupSaveDirectory(server);
            destPathFolderName = destPathFile.getName();
            if (!destPathFile.mkdir() && !destPathFile.isDirectory()) {
                message(context, String.format("Failed to create backup saving directory: %s. Failed to backup.", destPathFolderName));
                return FAILED;
            }

            // Make zip
            String levelPath = getLevelPath(server);
            debug(String.format("zip(srcPath=%s, destPath=%s)", levelPath, destPathFile.toString()));
            ZipUtil.zip(levelPath, destPathFile.toString(), getBackupFileName(backupName));

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

    private static String getBackupFileName(String backupName) {
        return backupFileNamePrefix + backupName + ".zip";
    }

    private static String getBackupName(String backupFileName) {
        try {
            if (backupFileName.matches(backupFileNamePrefix + ".+\\.zip"))
                return backupFileName.substring(backupFileNamePrefix.length(), backupFileName.length() - 4);
        } catch (IndexOutOfBoundsException ignored) {
        }
        return backupFileName;
    }

    private static File getBackupSaveDirectory(MinecraftServer server) {
        return new File(server.getRunDirectory(), backupSaveDirectoryName);
    }

    private static String getLevelPath(MinecraftServer server) {
        return (new File(server.getRunDirectory(), server.getLevelName())).getAbsolutePath();
    }

    /**
     * Restore with context parameter backupName.
     *
     * @param context the context.
     * @return stat code.
     */
    public static int confirm(CommandContext<ServerCommandSource> context) {
        if (restoreBackupNameToBeConfirmed == null) {
            message(context, "Nothing to be confirmed. Please execute /kb restore <backup_name> first.");
            return FAILED;
        }

        // do restore to backupName
        String backupName = restoreBackupNameToBeConfirmed;
        message(context, String.format("Restoring worlds to %s ...", backupName), true);

        // Get server
        MinecraftServer server = context.getSource().getMinecraftServer();
        String backupFileName = getBackupFileName(backupName);
        debug("Backup file name: " + backupFileName);
        File backupFile = new File(getBackupSaveDirectory(server), backupFileName);
        PostProgressRestoreThread postProgressRestoreThread = new PostProgressRestoreThread(server.getThread(), backupFile.getPath(), getLevelPath(server));
        Thread postThread = new Thread(postProgressRestoreThread, "PostProgressRestoreThread");
        postThread.start();
        server.stop(false);
        message(context, "Decompressing archive data. Server will shutdown to replace level data. Please do not restart the server.", true);
        return SUCCESS;
    }

    static boolean opPermissionValidator(ServerCommandSource commandSource) {
        return commandSource.hasPermissionLevel(4);
    }



}
