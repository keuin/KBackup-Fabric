package com.keuin.kbackupfabric.util;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.network.MessageType;
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

import java.util.UUID;


public final class PrintUtil {

    private static final Object syncMessage = new Object();
    private static final Object syncBroadcast = new Object();

    private static final Style broadcastStyle = Style.EMPTY.withColor(Formatting.AQUA);
    private static final Style infoStyle = Style.EMPTY.withColor(Formatting.WHITE);
    private static final Style stressStyle = Style.EMPTY.withColor(Formatting.AQUA);
    private static final Style warnStyle = Style.EMPTY.withColor(Formatting.YELLOW);
    private static final Style errorStyle = Style.EMPTY.withColor(Formatting.DARK_RED);

    private static final Logger LOGGER = LogManager.getLogger();
    private static PlayerManager fuckingPlayerManager = null;

    public static void setPlayerManager(PlayerManager playerManager) {
        if (fuckingPlayerManager == null)
            fuckingPlayerManager = playerManager;
    }

    public static void broadcast(String message) {
        broadcast(message, broadcastStyle);
    }

    public static void broadcast(String message, Style style) {
        synchronized (syncBroadcast) {
            if (fuckingPlayerManager != null)
//                fuckingPlayerManager.sendToAll(new LiteralText(message).setStyle(broadcastStyle));
                fuckingPlayerManager.broadcast(new LiteralText(message).setStyle(broadcastStyle), MessageType.GAME_INFO, UUID.randomUUID());
            else
                PrintUtil.error("Error in PrintUtil.broadcast: PlayerManager is not initialized.");
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

    private static CommandContext<ServerCommandSource> message(CommandContext<ServerCommandSource> context, String messageText, boolean broadcastToOps, Style style) {
        if (context != null) {
            synchronized (syncMessage) {
                Text text = new LiteralText(messageText).setStyle(style);
                context.getSource().sendFeedback(text, broadcastToOps);
            }
        } else {
            broadcast(messageText, style);
        }
        return context;
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
