package com.keuin.kbackupfabric;

import com.keuin.kbackupfabric.util.backup.suggestion.BackupMethodSuggestionProvider;
import com.keuin.kbackupfabric.util.backup.suggestion.BackupNameSuggestionProvider;
import com.keuin.kbackupfabric.util.PermissionValidator;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public final class KBCommandsRegister {
    // First make method to register
    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {

        // register /kb and /kb help for help menu
        dispatcher.register(CommandManager.literal("kb").executes(KBCommands::kb));
        dispatcher.register(CommandManager.literal("kb").then(CommandManager.literal("help").executes(KBCommands::help)));

        // register /kb list for showing the backup list. OP is required.
        dispatcher.register(CommandManager.literal("kb").then(CommandManager.literal("list").requires(PermissionValidator::op).executes(KBCommands::list)));

        // register /kb backup zip [name] as a alias
        dispatcher.register(CommandManager.literal("kb").then(CommandManager.literal("backup").then(
                CommandManager.literal("zip").then(
                        CommandManager.argument("backupName", StringArgumentType.greedyString()).requires(PermissionValidator::op).executes(KBCommands::primitiveBackup)
                ).requires(PermissionValidator::op).executes(KBCommands::primitiveBackupWithDefaultName)))
        );

        // register /kb backup incremental [name]
        dispatcher.register(CommandManager.literal("kb").then(CommandManager.literal("backup").then(
                CommandManager.literal("incremental").then(
                        CommandManager.argument("backupName", StringArgumentType.greedyString()).requires(PermissionValidator::op).executes(KBCommands::incrementalBackup)
                ).requires(PermissionValidator::op).executes(KBCommands::incrementalBackupWithDefaultName)))
        );

        // register /kb backup [name] for performing backup. OP is required.
        dispatcher.register(CommandManager.literal("kb").then(CommandManager.literal("backup").then(
                CommandManager.argument("backupName", StringArgumentType.greedyString()).requires(PermissionValidator::op).executes(KBCommands::primitiveBackup)
        ).requires(PermissionValidator::op).executes(KBCommands::primitiveBackupWithDefaultName)));

        // register /kb restore <name> for performing restore. OP is required.
        dispatcher.register(CommandManager.literal("kb").then(CommandManager.literal("restore").then(CommandManager.argument("backupName", StringArgumentType.greedyString()).suggests(BackupNameSuggestionProvider.getProvider()).requires(PermissionValidator::op).executes(KBCommands::restore)).executes(KBCommands::list)));

        // register /kb delete [name] for deleting an existing backup. OP is required.
        dispatcher.register(CommandManager.literal("kb").then(CommandManager.literal("delete").then(CommandManager.argument("backupName", StringArgumentType.greedyString()).suggests(BackupNameSuggestionProvider.getProvider()).requires(PermissionValidator::op).executes(KBCommands::delete))));

        // register /kb confirm for confirming the execution. OP is required.
        dispatcher.register(CommandManager.literal("kb").then(CommandManager.literal("confirm").requires(PermissionValidator::op).executes(KBCommands::confirm)));

        // register /kb cancel for cancelling the execution to be confirmed. OP is required.
        dispatcher.register(CommandManager.literal("kb").then(CommandManager.literal("cancel").requires(PermissionValidator::op).executes(KBCommands::cancel)));

        // register /kb prev for showing the latest backup.
        dispatcher.register(CommandManager.literal("kb").then(CommandManager.literal("prev").requires(PermissionValidator::op).executes(KBCommands::prev)));

        // register /kb setMethod for selecting backup method (zip, incremental)
        dispatcher.register(CommandManager.literal("kb").then(CommandManager.literal("setMethod").then(CommandManager.argument("backupMethod", StringArgumentType.string()).suggests(BackupMethodSuggestionProvider.getProvider()).requires(PermissionValidator::op).executes(KBCommands::setMethod))));
    }
}
