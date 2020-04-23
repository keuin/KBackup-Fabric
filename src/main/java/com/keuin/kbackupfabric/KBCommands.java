package com.keuin.kbackupfabric;

import com.keuin.kbackupfabric.data.BackupMetadata;
import com.keuin.kbackupfabric.util.PrintUtil;
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
import static com.keuin.kbackupfabric.util.PrintUtil.*;

public final class KBCommands {


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
        msgInfo(context, "==== KBackup Manual ====");
        msgInfo(context, "/kb       /kb help        Print help menu.");
        msgInfo(context, "/kb list                  Show all backups.");
        msgInfo(context, "/kb backup [backup_name]  Backup the whole level to backup_name. The default name is current system time.");
        msgInfo(context, "/kb restore <backup_name> Delete the whole current level and restore from given backup. /kb restore is identical with /kb list.");
        msgInfo(context, "/kb confirm               Confirm and start restoring.");
        msgInfo(context, "/kb cancel                Cancel the restoration to be confirmed. If cancelled, /kb confirm will not run.");
        return SUCCESS;
    }

    public static int list(CommandContext<ServerCommandSource> context) {
        msgInfo(context, "Available backups: (file is not checked, manipulation may affect this plugin)");
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
                msgInfo(context, String.format("[%d] %s, size: %.1fMB", i, backupName, file.length() * 1.0 / 1024 / 1024));
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
            msgWarn(context, String.format("Pure numeric name is not allowed. Renamed to %s", backupName));
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
            msgErr(context, "Invalid backup name! Please check your input. The list index number is also valid.", false);
            return FAILED;
        }

        // Update confirm pending variable
        restoreBackupNameToBeConfirmed = backupName;
        msgWarn(context, String.format("WARNING: You will LOST YOUR CURRENT WORLD COMPLETELY! It will be replaced with the backup %s . Please use /kb confirm to proceed executing.", restoreBackupNameToBeConfirmed), true);
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
        BackupMetadata metadata = new BackupMetadata(System.currentTimeMillis(), backupName);
        BackupWorker.invoke(context, backupName, metadata);
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
            msgInfo(context, "Nothing to be confirmed. Please execute /kb restore <backup_name> first.");
            return FAILED;
        }

        // do restore to backupName
        String backupName = restoreBackupNameToBeConfirmed;
        PrintUtil.msgInfo(context, String.format("Restoring worlds to %s ...", backupName), true);

        // Get server
        MinecraftServer server = context.getSource().getMinecraftServer();
        String backupFileName = getBackupFileName(backupName);
        debug("Backup file name: " + backupFileName);
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
            PrintUtil.msgInfo(context, "The restoration is cancelled.", true);
            return SUCCESS;
        } else {
            msgErr(context, "Nothing to cancel.");
            return FAILED;
        }
    }
}
