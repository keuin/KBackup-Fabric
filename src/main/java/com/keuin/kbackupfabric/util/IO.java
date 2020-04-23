package com.keuin.kbackupfabric.util;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IO {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final boolean printDebugMessages = true;
    private static final boolean printErrorMessages = true;
    private static final boolean printInfoMessages = true;
    private static final Object syncDebug = new Object();
    private static final Object syncError = new Object();
    private static final Object syncInfo = new Object();

    public static CommandContext<ServerCommandSource> message(CommandContext<ServerCommandSource> context, String messageText) {
        return message(context, messageText, false);
    }

    public static CommandContext<ServerCommandSource> message(CommandContext<ServerCommandSource> context, String messageText, boolean broadcastToOps) {
        context.getSource().sendFeedback(new LiteralText("[KBackup] " + messageText), broadcastToOps);
        return context;
    }

    public static void debug(String message) {
        synchronized (syncDebug) {
            if (printDebugMessages) {
                System.out.println(String.format("[DEBUG] [KBackup] %s", message));
                LOGGER.debug(message);
            }
        }
    }

    public static void error(String message) {
        synchronized (syncError) {
            if (printErrorMessages) {
                System.out.println(String.format("[ERROR] [KBackup] %s", message));
                LOGGER.error(message);
            }
        }
    }

    public static void info(String message) {
        synchronized (syncInfo) {
            if (printInfoMessages) {
                System.out.println(String.format("[INFO] [KBackup] %s", message));
                LOGGER.info(message);
            }
        }
    }
}
