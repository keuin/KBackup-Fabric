package com.keuin.kbackupfabric.operation;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;

public abstract class Confirmable {

    public static Confirmable createRestoreOperation(CommandContext<ServerCommandSource> context, String backupName) {
        return new RestoreOperation(context, backupName);
    }

    public static Confirmable createDeleteOperation(CommandContext<ServerCommandSource> context, String backupName) {
        return new DeleteOperation(context, backupName);
    }

    public abstract boolean confirm();

    @Override
    public abstract String toString();
}
