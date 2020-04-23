package com.keuin.kbackupfabric;

import com.keuin.kbackupfabric.worker.BackupWorker;
import com.keuin.kbackupfabric.worker.RestoreWorker;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import static com.keuin.kbackupfabric.util.BackupFilesystemUtil.*;
import static com.keuin.kbackupfabric.util.PrintUtil.debug;
import static com.keuin.kbackupfabric.util.PrintUtil.message;

public final class KBCommandHandler {


    private static final int SUCCESS = 1;
    private static final int FAILED = -1;


    private static final HashMap<Integer, String> backupIndexNameMapper = new HashMap<>(); // index -> backupName
    private static String restoreBackupNameToBeConfirmed = null;

    /**
     * Print the help menu.
     *
     * @param context the context.
     * @return stat code.
     */
    public static int help(CommandContext<ServerCommandSource> context) {
        message(context, "==== KBackup Manual ====");
        message(context, "/kb   /kb help            Print help menu.");
        message(context, "/kb list                  Show all backups.");
        message(context, "/kb backup [backup_name]  Backup world, nether, end to backup_name. By default, the name is current system time.");
        message(context, "/kb restore <backup_name> Delete current three worlds, restore the older version from given backup. By default, this command is identical with /kb list.");
        message(context, "/kb confirm               Confirm and start restoring.");
        message(context, "/kb cancel                Cancel the restoration to be confirmed. If cancelled, /kb confirm will not effect without another valid /kb restore command.");
        return SUCCESS;
    }

    public static int list(CommandContext<ServerCommandSource> context) {
        message(context, "Available backups: (file is not checked, manipulation may affect this plugin)");
        MinecraftServer server = context.getSource().getMinecraftServer();
        File[] files = getBackupSaveDirectory(server).listFiles(
                (dir, name) -> dir.isDirectory() && name.toLowerCase().endsWith(".zip") && name.toLowerCase().startsWith(getBackupFileNamePrefix())
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
        MinecraftServer server = context.getSource().getMinecraftServer();
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

        // Validate backupName
        if (!isBackupNameValid(backupName, server)) {
            // Invalid backupName
            message(context, "Invalid backup name! Please check your input. The list index number is also valid.", false);
            return FAILED;
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
        BackupWorker.invoke(context, backupName);
        return SUCCESS;
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
        message(context, "Server will shutdown in a few seconds, depended on your world size and the disk speed, the restore progress may take seconds or minutes.", true);
        message(context, "Please do not force the server stop, or the level would be broken.", true);
        message(context, "After it shuts down, please restart the server manually.", true);
        final int WAIT_SECONDS = 10;
        for (int i = 0; i < WAIT_SECONDS; ++i) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
        message(context, "Shutting down ...", true);
        RestoreWorker.invoke(server, backupFile.getPath(), getLevelPath(server));
        return SUCCESS;
    }

    /**
     * Cancel the execution to be confirmed.
     *
     * @param context the context.
     * @return stat code.
     */
    public static int cancel(CommandContext<ServerCommandSource> context) {
        if (restoreBackupNameToBeConfirmed != null) {
            restoreBackupNameToBeConfirmed = null;
            message(context, "The restoration is cancelled.", true);
            return SUCCESS;
        } else {
            message(context, "Nothing to cancel.");
            return FAILED;
        }
    }
}
