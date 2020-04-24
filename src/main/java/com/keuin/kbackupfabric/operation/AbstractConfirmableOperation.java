package com.keuin.kbackupfabric.operation;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;

public abstract class AbstractConfirmableOperation {

    public static AbstractConfirmableOperation createRestoreOperation(CommandContext<ServerCommandSource> context, String backupName) {
        return new RestoreOperation(context, backupName);
    }

    public static AbstractConfirmableOperation createDeleteOperation(CommandContext<ServerCommandSource> context, String backupName) {
        return new DeleteOperation(context, backupName);
    }

    public abstract boolean confirm();

    @Override
    public abstract String toString();
}
