package com.keuin.kbackupfabric.util;

import net.minecraft.server.command.ServerCommandSource;

public class PermissionValidator {
    public static boolean op(ServerCommandSource commandSource) {
        return commandSource.hasPermissionLevel(4);
    }
}
