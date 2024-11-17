package com.keuin.kbackupfabric.ui;

import com.keuin.kbackupfabric.backup.BackupFilesystemUtil;
import com.keuin.kbackupfabric.backup.name.IncrementalBackupFileNameEncoder;
import com.keuin.kbackupfabric.backup.name.PrimitiveBackupFileNameEncoder;
import com.keuin.kbackupfabric.backup.suggestion.BackupNameSuggestionProvider;
import com.keuin.kbackupfabric.config.KBackupConfig;
import com.keuin.kbackupfabric.metadata.MetadataHolder;
import com.keuin.kbackupfabric.operation.BackupOperation;
import com.keuin.kbackupfabric.operation.DeleteOperation;
import com.keuin.kbackupfabric.operation.RestoreOperation;
import com.keuin.kbackupfabric.operation.abstracts.i.Invokable;
import com.keuin.kbackupfabric.operation.backup.method.ConfiguredBackupMethod;
import com.keuin.kbackupfabric.operation.backup.method.ConfiguredIncrementalBackupMethod;
import com.keuin.kbackupfabric.operation.backup.method.ConfiguredPrimitiveBackupMethod;
import com.keuin.kbackupfabric.util.DateUtil;
import com.keuin.kbackupfabric.util.PrintUtil;
import com.keuin.kbackupfabric.util.cow.FileCowCopier;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.keuin.kbackupfabric.backup.BackupFilesystemUtil.*;
import static com.keuin.kbackupfabric.util.PrintUtil.*;

public final class KBCommands {


    private static final int SUCCESS = 1;
    private static final int FAILED = -1;
    private static final String DEFAULT_BACKUP_NAME = "noname";
    private static boolean notifiedPreviousRestoration = false;

    // don't access it directly
    private static MinecraftServer server;
    private static BackupManager backupManager;
    private static final Object managerCreatorLock = new Object();
    private static final List<BackupInfo> backupList = new ArrayList<>(); // index -> backupName
    private static Invokable pendingOperation = null;

    public static void setServer(MinecraftServer server) {
        KBCommands.server = server;
    }

    private static MinecraftServer getServer() {
        if (server != null)
            return server;
        throw new IllegalStateException("server is not initialized.");
    }

    private static BackupManager getBackupManager() {
        synchronized (managerCreatorLock) {
            if (backupManager == null)
                backupManager = new BackupManager(getBackupSaveDirectory(getServer()));
            return backupManager;
        }
    }

    /**
     * Print the help menu.
     *
     * @param context the context.
     * @return stat code.
     */
    public static int help(CommandContext<ServerCommandSource> context) {
        msgInfo(context, "==== KBackup Manual ====");
        msgInfo(context, "/kb , /kb help - Print help menu.");
        msgInfo(context, "/kb list - Show all backups.");
        msgInfo(context, "/kb backup [incremental/zip] [backup_name] - Backup the whole level to backup_name. The default name is current system time.");
        msgInfo(context, "/kb restore <backup_name> - Delete the whole current level and restore from given backup. /kb restore is identical with /kb list.");
        msgInfo(context, "/kb confirm - Confirm and start restoring.");
        msgInfo(context, "/kb cancel - Cancel the restoration to be confirmed. If cancelled, /kb confirm will not run.");
        msgInfo(context, "/kb cow-info - Display copy-on-write support info (Experimental)");
        return SUCCESS;
    }

    /**
     * Print the help menu. (May show extra info during the first run after restoring)
     *
     * @param context the context.
     * @return stat code.
     */
    public static int kb(CommandContext<ServerCommandSource> context) {
        int statCode = list(context);
        if (MetadataHolder.hasMetadata() && !notifiedPreviousRestoration) {
            // Output metadata info
            notifiedPreviousRestoration = true;
            msgStress(context, "Restored from backup "
                    + MetadataHolder.getMetadata().getBackupName() + " (created at " +
                    DateUtil.fromEpochMillis(MetadataHolder.getMetadata().getBackupTime())
                    + ")");
        }
        return statCode;
    }

    private static void updateBackupList() {
        synchronized (backupList) {
            backupList.clear();
            List<BackupInfo> list = new ArrayList<>();
            getBackupManager().getAllBackups().forEach(list::add);
            list.sort(Comparator.comparing(BackupInfo::getCreationTime).reversed());
            backupList.addAll(list);
        }
    }

    /**
     * List all existing backups.
     *
     * @param context the context.
     * @return stat code.
     */
    public static int list(CommandContext<ServerCommandSource> context) {
        // lazy: it just works as expected. Don't try to refactor, it's a waste of time. Just improve display and
        //       that's enough.
        // TODO: Show real name and size and etc info for incremental backup
        // TODO: Show concrete info from metadata for `.zip` backup
//        MinecraftServer server = context.getSource().getMinecraftServer();
        // TODO: refactor this to use {@link ObjectCollectionSerializer#fromDirectory}

        updateBackupList();
        synchronized (backupList) {
            if (backupList.isEmpty())
                msgInfo(context, "There is no backup available. To make a new backup, use `/kb backup`.");
            else
                msgInfo(context, "Available backups:");
            for (int i = backupList.size() - 1; i >= 0; --i) {
                BackupInfo info = backupList.get(i);
                printBackupInfo(context, info, i);
            }
        }
        return SUCCESS;
    }

    public static int cowInfo(CommandContext<ServerCommandSource> context) {
        try {
            msgInfo(context, "KBackup-cow library version: " + FileCowCopier.getVersion());
            msgInfo(context, "CoW enabled: " + KBackupConfig.getInstance().getIncbakCow());
        } catch (Exception | UnsatisfiedLinkError ignored) {
            msgErr(context, "KBackup-cow library is not loaded");
        }
        return SUCCESS;
    }

    /**
     * Print backup information.
     *
     * @param context the context.
     * @param info    the info.
     * @param i       the index, starting from 0.
     */
    private static void printBackupInfo(CommandContext<ServerCommandSource> context, BackupInfo info, int i) {
        msgInfo(context, String.format(
                "[%d] (%s) %s (%s) %s",
                i + 1,
                info.getType(),
                info.getName(),
                DateUtil.getPrettyString(info.getCreationTime()),
                (info.getSizeBytes() > 0) ? BackupFilesystemUtil.getFriendlyFileSizeString(info.getSizeBytes()) : ""
        ));
    }

    /**
     * Backup with context parameter backupName.
     *
     * @param context the context.
     * @return stat code.
     */
    public static int primitiveBackup(CommandContext<ServerCommandSource> context) {
        //KBMain.backup("name")
        String customBackupName = StringArgumentType.getString(context, "backupName");
        if (customBackupName.matches("[0-9]*")) {
            // Numeric param is not allowed
            customBackupName = String.format("a%s", customBackupName);
            msgWarn(context, String.format("Pure numeric name is not allowed. Renaming to %s", customBackupName));
        }
        return doBackup(context, customBackupName, false);
    }

    /**
     * Backup with default name.
     *
     * @param context the context.
     * @return stat code.
     */
    public static int primitiveBackupWithDefaultName(CommandContext<ServerCommandSource> context) {
        return doBackup(context, DEFAULT_BACKUP_NAME, false);
    }

    public static int incrementalBackup(CommandContext<ServerCommandSource> context) {
        String customBackupName = StringArgumentType.getString(context, "backupName");
        if (customBackupName.matches("[0-9]*")) {
            // Numeric param is not allowed
            customBackupName = String.format("a%s", customBackupName);
            msgWarn(context, String.format("Pure numeric name is not allowed. Renaming to %s", customBackupName));
        }
        return doBackup(context, customBackupName, true);
    }

    public static int incrementalBackupWithDefaultName(CommandContext<ServerCommandSource> context) {
        return doBackup(context, DEFAULT_BACKUP_NAME, true);
    }


    /**
     * Delete an existing backup with context parameter backupName.
     * Simply set the pending backupName to given backupName, for the second confirmation.
     *
     * @param context the context.
     * @return stat code.
     */
    public static int delete(CommandContext<ServerCommandSource> context) {

        String backupFileName = parseBackupFileName(context, StringArgumentType.getString(context, "backupName"));
        MinecraftServer server = context.getSource().getMinecraftServer();

        if (backupFileName == null)
            return list(context); // Show the list and return

        // Validate backupName
        if (!isBackupFileExists(backupFileName, server)) {
            // Invalid backupName
            msgErr(context, "Invalid backup name! Please check your input. The list index number is also valid.");
            return FAILED;
        }

        // Update pending task
        //pendingOperation = AbstractConfirmableOperation.createDeleteOperation(context, backupName);
        pendingOperation = new DeleteOperation(context, backupFileName);

        msgWarn(context, String.format("DELETION WARNING: The deletion is irreversible! You will lose the backup %s permanently. Use /kb confirm to start or /kb cancel to abort.", backupFileName), true);
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
        try {
            //KBMain.restore("name")
            MinecraftServer server = context.getSource().getMinecraftServer();
            String backupFileName = parseBackupFileName(context, StringArgumentType.getString(context, "backupName"));
//            backupFileName = parseBackupFileName(context, backupFileName);

            if (backupFileName == null)
                return list(context); // Show the list and return

            // Validate backupName
            if (!isBackupFileExists(backupFileName, server)) {
                // Invalid backupName
                msgErr(context, "Invalid backup name! Please check your input. The list index number is also valid.", false);
                return FAILED;
            }

            // Detect backup type

            // Update pending task
            //pendingOperation = AbstractConfirmableOperation.createRestoreOperation(context, backupName);
//        File backupFile = new File(getBackupSaveDirectory(server), getBackupFileName(backupName));
            // TODO: improve this
            ConfiguredBackupMethod method = backupFileName.endsWith(".zip") ?
                    new ConfiguredPrimitiveBackupMethod(
                            backupFileName, getLevelPath(server), getBackupSaveDirectory(server).getAbsolutePath()
                    ) : new ConfiguredIncrementalBackupMethod(
                    backupFileName, getLevelPath(server),
                    getBackupSaveDirectory(server).getAbsolutePath(),
                    getIncrementalBackupBaseDirectory(server).getAbsolutePath()
            );
            // String backupSavePath, String levelPath, String backupFileName
//        getBackupSaveDirectory(server).getAbsolutePath(), getLevelPath(server), backupFileName
            pendingOperation = new RestoreOperation(context, method);

            msgWarn(context, String.format("RESET WARNING: You will LOSE YOUR CURRENT WORLD PERMANENTLY! The worlds will be replaced with backup %s . Use /kb confirm to start or /kb cancel to abort.", backupFileName), true);
            return SUCCESS;
        } catch (IOException e) {
            msgErr(context, String.format("An I/O exception occurred while making backup: %s", e));
        }
        return FAILED;
    }

    private static int doBackup(CommandContext<ServerCommandSource> context, String customBackupName, boolean incremental) {
        try {
            // Real backup name (compatible with legacy backup): date_name, such as 2020-04-23_21-03-00_test
            //KBMain.backup("name")
//        String backupName = BackupNameTimeFormatter.getTimeString() + "_" + customBackupName;

            // Validate file name
            final char[] ILLEGAL_CHARACTERS = {'/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':'};
            for (char c : ILLEGAL_CHARACTERS) {
                if (customBackupName.contains(String.valueOf(c))) {
                    msgErr(context, String.format("Name cannot contain special character \"%c\".", c));
                    return FAILED;
                }
            }

            PrintUtil.info("Start backup...");

            // configure backup method
            MinecraftServer server = context.getSource().getMinecraftServer();
            ConfiguredBackupMethod method = !incremental ? new ConfiguredPrimitiveBackupMethod(
                    PrimitiveBackupFileNameEncoder.INSTANCE.encode(customBackupName, LocalDateTime.now()),
                    getLevelPath(server),
                    getBackupSaveDirectory(server).getCanonicalPath()
            ) : new ConfiguredIncrementalBackupMethod(
                    IncrementalBackupFileNameEncoder.INSTANCE.encode(customBackupName, LocalDateTime.now()),
                    getLevelPath(server),
                    getBackupSaveDirectory(server).getCanonicalPath(),
                    getIncrementalBackupBaseDirectory(server).getCanonicalPath()
            );

            // dispatch to operation worker
            BackupOperation operation = new BackupOperation(context, method);
            if (operation.invoke()) {
                return SUCCESS;
            } else if (operation.isBlocked()) {
                msgWarn(context, "Another task is running, cannot issue new backup at once.");
                return FAILED;
            }
        } catch (IOException e) {
            msgErr(context, String.format("An I/O exception occurred while making backup: %s", e));
        }
        return FAILED;
    }

    /**
     * Restore with context parameter backupName.
     *
     * @param context the context.
     * @return stat code.
     */
    public static int confirm(CommandContext<ServerCommandSource> context) {
        if (pendingOperation == null) {
            msgWarn(context, "Nothing to confirm.");
            return FAILED;
        }

        Invokable operation = pendingOperation;
        pendingOperation = null;

        boolean returnValue = operation.invoke();

        // By the way, update suggestion list.
        BackupNameSuggestionProvider.updateCandidateList();

        return returnValue ? SUCCESS : FAILED; // block compiler's complaint.
    }

    /**
     * Cancel the execution to be confirmed.
     *
     * @param context the context.
     * @return stat code.
     */
    public static int cancel(CommandContext<ServerCommandSource> context) {
        if (pendingOperation != null) {
            PrintUtil.msgInfo(context, String.format("The %s has been cancelled.", pendingOperation), true);
            pendingOperation = null;
            return SUCCESS;
        } else {
            msgErr(context, "Nothing to cancel.");
            return FAILED;
        }
    }

    /**
     * Show the most recent backup.
     * If there is no available backup, print specific info.
     *
     * @param context the context.
     * @return stat code.
     */
    public static int prev(CommandContext<ServerCommandSource> context) {
        // FIXME: This breaks after adding incremental backup
        try {
            // List all backups
            updateBackupList();
            synchronized (backupList) {
                if (!backupList.isEmpty()) {
                    BackupInfo info = backupList.get(0);
                    msgInfo(context, "The most recent backup:");
                    printBackupInfo(context, info, 0);
                } else {
                    msgInfo(context, "There is no backup available.");
                }
            }
        } catch (SecurityException ignored) {
            msgErr(context, "Failed to read file.");
            return FAILED;
        }
        return SUCCESS;
    }


    private static String parseBackupFileName(CommandContext<ServerCommandSource> context, String userInput) {
        try {
            String backupName = StringArgumentType.getString(context, "backupName");

            if (backupName.matches("[0-9]*")) {
                // treat numeric input as backup index number in list
                int index = Integer.parseInt(backupName) - 1;
                synchronized (backupList) {
                    return backupList.get(index).getBackupFileName(); // Replace input number with real backup file name.
                }
            }
        } catch (NumberFormatException | IndexOutOfBoundsException ignored) {
        }
        return userInput;
    }
}
