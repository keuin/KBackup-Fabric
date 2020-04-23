package com.keuin.kbackupfabric;

import com.keuin.kbackupfabric.util.PermissionValidator;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public final class KBRegister {
    // First make method to register
    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        // register /kb and /kb help for help menu
        dispatcher.register(CommandManager.literal("kb").executes(KBCommands::help));
        dispatcher.register(CommandManager.literal("kb").then(CommandManager.literal("help").executes(KBCommands::help)));

        // register /kb list for showing the backup list. OP is required.
        dispatcher.register(CommandManager.literal("kb").then(CommandManager.literal("list").requires(PermissionValidator::op).executes(KBCommands::list)));

        // register /kb backup [name] for performing backup. OP is required.
        dispatcher.register(CommandManager.literal("kb").then(CommandManager.literal("backup").then(CommandManager.argument("backupName", StringArgumentType.string()).requires(PermissionValidator::op).executes(KBCommands::backup)).requires(PermissionValidator::op).executes(KBCommands::backupWithDefaultName)));

        // register /kb restore <name> for performing restore. OP is required.
        dispatcher.register(CommandManager.literal("kb").then(CommandManager.literal("restore").then(CommandManager.argument("backupName", StringArgumentType.string()).requires(PermissionValidator::op).executes(KBCommands::restore)).executes(KBCommands::list)));

        // register /kb confirm for confirming the execution. OP is required.
        dispatcher.register(CommandManager.literal("kb").then(CommandManager.literal("confirm").requires(PermissionValidator::op).executes(KBCommands::confirm)));

        // register /kb cancel for cancelling the execution to be confirmed. OP is required.
        dispatcher.register(CommandManager.literal("kb").then(CommandManager.literal("cancel").requires(PermissionValidator::op).executes(KBCommands::cancel)));

    }
}
