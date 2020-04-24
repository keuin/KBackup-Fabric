package com.keuin.kbackupfabric.util;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public final class PrintUtil {

    private static final Object syncMessage = new Object();

    private static final Style infoStyle = new Style().setColor(Formatting.WHITE);
    private static final Style warnStyle = new Style().setColor(Formatting.YELLOW);
    private static final Style errorStyle = new Style().setColor(Formatting.DARK_RED);

    private static final Logger LOGGER = LogManager.getLogger();

    public static CommandContext<ServerCommandSource> msgInfo(CommandContext<ServerCommandSource> context, String messageText) {
        return msgInfo(context, messageText, false);
    }

    public static CommandContext<ServerCommandSource> msgWarn(CommandContext<ServerCommandSource> context, String messageText) {
        return msgWarn(context, messageText, false);
    }

    public static CommandContext<ServerCommandSource> msgErr(CommandContext<ServerCommandSource> context, String messageText) {
        return msgErr(context, messageText, false);
    }

    public static CommandContext<ServerCommandSource> msgInfo(CommandContext<ServerCommandSource> context, String messageText, boolean broadcastToOps) {
        return message(context, messageText, broadcastToOps, infoStyle);
    }

    public static CommandContext<ServerCommandSource> msgWarn(CommandContext<ServerCommandSource> context, String messageText, boolean broadcastToOps) {
        return message(context, messageText, broadcastToOps, warnStyle);
    }

    public static CommandContext<ServerCommandSource> msgErr(CommandContext<ServerCommandSource> context, String messageText, boolean broadcastToOps) {
        return message(context, messageText, broadcastToOps, errorStyle);
    }

    private static CommandContext<ServerCommandSource> message(CommandContext<ServerCommandSource> context, String messageText, boolean broadcastToOps, Style style) {
        synchronized (syncMessage) {
            Text text = new LiteralText(messageText);
            text.setStyle(style);
            context.getSource().sendFeedback(text, broadcastToOps);
        }
        return context;
    }

    public static void debug(String string) {
        LOGGER.debug("[KBackup] " + string);
    }

    public static void info(String string) {
        LOGGER.info("[KBackup] " + string);
    }

    public static void warn(String string) {
        LOGGER.warn("[KBackup] " + string);
    }

    public static void error(String string) {
        LOGGER.error("[KBackup] " + string);
    }
}
