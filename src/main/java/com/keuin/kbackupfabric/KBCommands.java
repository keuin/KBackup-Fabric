package com.keuin.kbackupfabric;

import com.keuin.kbackupfabric.data.BackupMetadata;
import com.keuin.kbackupfabric.data.PendingOperation;
import com.keuin.kbackupfabric.util.PrintUtil;
import com.keuin.kbackupfabric.worker.BackupWorker;
import com.keuin.kbackupfabric.worker.RestoreWorker;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import static com.keuin.kbackupfabric.util.BackupFilesystemUtil.*;
import static com.keuin.kbackupfabric.util.PrintUtil.*;
import static org.apache.commons.io.FileUtils.forceDelete;

public final class KBCommands {


    private static final int SUCCESS = 1;
    private static final int FAILED = -1;

    private static final Logger LOGGER = LogManager.getLogger();

    private static final HashMap<Integer, String> backupIndexNameMapper = new HashMap<>(); // index -> backupName
    private static PendingOperation pendingOperation = null;

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
            msgWarn(context, String.format("Pure numeric name is not allowed. Renaming to %s", backupName));
        }
        return doBackup(context, backupName);
    }

    /**
     * Delete an existing backup with context parameter backupName.
     * Simply set the pending backupName to given backupName, for the second confirmation.
     *
     * @param context the context.
     * @return stat code.
     */
    public static int delete(CommandContext<ServerCommandSource> context) {

        String backupName = parseBackupName(context, StringArgumentType.getString(context, "backupName"));
        MinecraftServer server = context.getSource().getMinecraftServer();

        if (backupName == null)
            return list(context); // Show the list and return

        // Validate backupName
        if (!isBackupNameValid(backupName, server)) {
            // Invalid backupName
            msgErr(context, "Invalid backup name! Please check your input. The list index number is also valid.");
            return FAILED;
        }

        // Update pending task
        pendingOperation = PendingOperation.deleteOperation(backupName);

        msgWarn(context, String.format("DELETION WARNING: The deletion is irreversible! You will lose the backup %s permanently. Use /kb confirm to start or /kb cancel to abort.", pendingOperation.getBackupName()), true);
        return SUCCESS;
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
        String backupName = parseBackupName(context, StringArgumentType.getString(context, "backupName"));
        backupName = parseBackupName(context, backupName);

        if (backupName == null)
            return list(context); // Show the list and return

        // Validate backupName
        if (!isBackupNameValid(backupName, server)) {
            // Invalid backupName
            msgErr(context, "Invalid backup name! Please check your input. The list index number is also valid.", false);
            return FAILED;
        }

        // Update pending task
        pendingOperation = PendingOperation.restoreOperation(backupName);

        msgWarn(context, String.format("RESET WARNING: You will LOSE YOUR CURRENT WORLD PERMANENTLY! The worlds will be replaced with backup %s . Use /kb confirm to start or /kb cancel to abort.", pendingOperation.getBackupName()), true);
        return SUCCESS;
    }


    /**
     * Backup with default name.
     *
     * @param context the context.
     * @return stat code.
     */
    public static int backupWithDefaultName(CommandContext<ServerCommandSource> context) {
        return doBackup(context, "noname");
    }

    private static int doBackup(CommandContext<ServerCommandSource> context, String customName) {
        // Real backup name (compatible with legacy backup): date_name, such as 2020-04-23_21-03-00_test
        //KBMain.backup("name")
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String timeString = LocalDateTime.now().format(formatter);
        String backupName = timeString + "_" + customName;

        // Validate file name
        final char[] ILLEGAL_CHARACTERS = {'/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':'};
        for (char c : ILLEGAL_CHARACTERS) {
            if (backupName.contains(String.valueOf(c))) {
                msgErr(context, String.format("Name cannot contain special character \"%c\".", c));
                return FAILED;
            }
        }

        // Do backup
        BackupMetadata metadata = new BackupMetadata(System.currentTimeMillis(), backupName);
        LOGGER.info("Invoking backup worker ...");
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
        if (pendingOperation == null) {
            msgWarn(context, "Nothing to be confirmed. Please execute /kb restore <backup_name> first.");
            return FAILED;
        }

        MinecraftServer server = context.getSource().getMinecraftServer();

        // Restore
        if (pendingOperation.isRestore()) {
            // do restore to backupName
            String backupName = pendingOperation.getBackupName();
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
            return SUCCESS;
        }

        // Delete
        if (pendingOperation.isDelete()) {
            String backupName = pendingOperation.getBackupName();
            String backupFileName = getBackupFileName(backupName);
            LOGGER.info("Deleting backup " + backupName);
            File backupFile = new File(getBackupSaveDirectory(server), backupFileName);
            int tryCounter = 0;
            do {
                if (tryCounter == 5) {
                    String msg = "Failed to delete file " + backupFileName;
                    LOGGER.error(msg);
                    msgErr(context, msg);
                    return FAILED;
                }
                try {
                    if (!backupFile.delete())
                        forceDelete(backupFile);
                } catch (SecurityException | NullPointerException | IOException ignored) {
                }
                ++tryCounter;
            } while (backupFile.exists());
            LOGGER.info("Deleted backup " + backupName);
            msgInfo(context, "Deleted backup " + backupName);
            return SUCCESS;
        }

        return SUCCESS; // block compiler's complain.
    }

    /**
     * Cancel the execution to be confirmed.
     *
     * @param context the context.
     * @return stat code.
     */
    public static int cancel(CommandContext<ServerCommandSource> context) {
        if (pendingOperation != null) {
            PrintUtil.msgInfo(context, String.format("The %s has been cancelled.", pendingOperation.toString()), true);
            pendingOperation = null;
            return SUCCESS;
        } else {
            msgErr(context, "Nothing to cancel.");
            return FAILED;
        }
    }

    private static String parseBackupName(CommandContext<ServerCommandSource> context, String userInput) {
        MinecraftServer server = context.getSource().getMinecraftServer();
        String backupName = StringArgumentType.getString(context, "backupName");

        if (backupName.matches("[0-9]*")) {
            // If numeric input
            Integer index = Integer.parseInt(backupName);
            return backupIndexNameMapper.get(index); // Replace input number with real backup name.
        }
        return userInput;
    }
}