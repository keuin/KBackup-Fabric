package com.keuin.kbackupfabric.util;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;


public final class PrintUtil {

    private static final Object syncMessage = new Object();
    private static final Object syncBroadcast = new Object();

    private static final Style broadcastStyle = new Style().setColor(Formatting.AQUA);
    private static final Style infoStyle = new Style().setColor(Formatting.WHITE);
    private static final Style stressStyle = new Style().setColor(Formatting.AQUA);
    private static final Style warnStyle = new Style().setColor(Formatting.YELLOW);
    private static final Style errorStyle = new Style().setColor(Formatting.DARK_RED);

    private static final Logger LOGGER = LogManager.getLogger();
    private static PlayerManager playerManager = null;

    public static void setPlayerManager(PlayerManager playerManager) {
        if (PrintUtil.playerManager == null)
            PrintUtil.playerManager = playerManager;
    }

    public static void broadcast(String message) {
        broadcast(message, broadcastStyle);
    }

    public static void broadcast(String message, Style style) {
        synchronized (syncBroadcast) {
            Optional.ofNullable(playerManager)
                    .ifPresent(pm ->
                            pm.sendToAll(new LiteralText(message).setStyle(style)));
        }
    }

    public static CommandContext<ServerCommandSource> msgStress(@Nullable CommandContext<ServerCommandSource> context, String messageText) {
        return msgStress(context, messageText, false);
    }

    public static CommandContext<ServerCommandSource> msgInfo(@Nullable CommandContext<ServerCommandSource> context, String messageText) {
        return msgInfo(context, messageText, false);
    }

    public static CommandContext<ServerCommandSource> msgWarn(@Nullable CommandContext<ServerCommandSource> context, String messageText) {
        return msgWarn(context, messageText, false);
    }

    public static CommandContext<ServerCommandSource> msgErr(@Nullable CommandContext<ServerCommandSource> context, String messageText) {
        return msgErr(context, messageText, false);
    }

    public static CommandContext<ServerCommandSource> msgStress(@Nullable CommandContext<ServerCommandSource> context, String messageText, boolean broadcastToOps) {
        return message(context, messageText, broadcastToOps, stressStyle);
    }

    public static CommandContext<ServerCommandSource> msgInfo(@Nullable CommandContext<ServerCommandSource> context, String messageText, boolean broadcastToOps) {
        return message(context, messageText, broadcastToOps, infoStyle);
    }

    public static CommandContext<ServerCommandSource> msgWarn(@Nullable CommandContext<ServerCommandSource> context, String messageText, boolean broadcastToOps) {
        return message(context, messageText, broadcastToOps, warnStyle);
    }

    public static CommandContext<ServerCommandSource> msgErr(@Nullable CommandContext<ServerCommandSource> context, String messageText, boolean broadcastToOps) {
        return message(context, messageText, broadcastToOps, errorStyle);
    }

    private static CommandContext<ServerCommandSource> message(@Nullable CommandContext<ServerCommandSource> context, String messageText, boolean broadcastToOps, Style style) {
        if (context != null) {
            synchronized (syncMessage) {
                Text text = new LiteralText(messageText);
                text.setStyle(style);
                context.getSource().sendFeedback(text, broadcastToOps);
            }
            return context;
        } else {
            // if context is null, then `broadcastToOps` will be ignored for simplicity
            broadcast(messageText, style);
            return null;
        }
    }

    /**
     * Print debug message on the server console.
     *
     * @param string the message.
     */
    public static void debug(String string) {
        LOGGER.debug("[KBackup] " + string);
    }

    /**
     * Print informative message on the server console.
     *
     * @param string the message.
     */
    public static void info(String string) {
        LOGGER.info("[KBackup] " + string);
    }

    /**
     * Print warning message on the server console.
     *
     * @param string the message.
     */
    public static void warn(String string) {
        LOGGER.warn("[KBackup] " + string);
    }

    /**
     * Print error message on the server console.
     *
     * @param string the message.
     */
    public static void error(String string) {
        LOGGER.error("[KBackup] " + string);
    }
}
