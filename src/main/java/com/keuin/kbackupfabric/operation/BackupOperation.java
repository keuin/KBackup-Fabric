package com.keuin.kbackupfabric.operation;

import com.keuin.kbackupfabric.operation.abstracts.InvokableAsyncBlockingOperation;
import com.keuin.kbackupfabric.operation.backup.feedback.BackupFeedback;
import com.keuin.kbackupfabric.operation.backup.method.ConfiguredBackupMethod;
import com.keuin.kbackupfabric.util.PrintUtil;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.World;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.keuin.kbackupfabric.util.PrintUtil.msgInfo;

public class BackupOperation extends InvokableAsyncBlockingOperation {

    private final CommandContext<ServerCommandSource> context;
    private final Map<World, Boolean> oldWorldsSavingDisabled = new HashMap<>();
    private final ConfiguredBackupMethod configuredBackupMethod;
    private long startTime;


    public BackupOperation(CommandContext<ServerCommandSource> context, ConfiguredBackupMethod configuredBackupMethod) {
        super("BackupWorker");
        this.context = context;
        this.configuredBackupMethod = configuredBackupMethod;
    }

    @Override
    protected void async() {
        String backupSaveDirectory = "";
        MinecraftServer server = context.getSource().getMinecraftServer();
        try {
            //// Do our main backup logic

            // Create backup saving directory
            if (!configuredBackupMethod.touch()) {
                PrintUtil.msgErr(context, "Failed to create backup save directory. Cannot backup.");
                return;
            }

            // Backup
            BackupFeedback result = configuredBackupMethod.backup();
            if (result.isSuccess()) {
                // Restore old auto-save switch stat
                server.getWorlds().forEach(world -> world.savingDisabled = oldWorldsSavingDisabled.getOrDefault(world, true));

                // Print finish message: time elapsed and file size
                long timeElapsedMillis = System.currentTimeMillis() - startTime;
                String msgText = String.format("Backup finished. Time elapsed: %.2fs. ", timeElapsedMillis / 1000.0) + result.getFeedback();
                PrintUtil.msgInfo(context, msgText, true);
            } else {
                // failed
                PrintUtil.msgErr(context, "Backup operation failed. No further information.");
            }
        } catch (SecurityException e) {
            msgInfo(context, String.format("Failed to create backup saving directory: %s. Failed to backup.", backupSaveDirectory));
        } catch (IOException e) {
            msgInfo(context, "Failed to make zip: " + e.getMessage());
        }
    }

    @Override
    protected boolean sync() {
        //// Save world, save old auto-save configs

        PrintUtil.broadcast("Making backup, please wait ...");

        // Get server
        MinecraftServer server = context.getSource().getMinecraftServer();

        // Save old auto-save switch state for restoration after finished
        oldWorldsSavingDisabled.clear();
        server.getWorlds().forEach(world -> {
            oldWorldsSavingDisabled.put(world, world.savingDisabled);
            world.savingDisabled = true;
        });

        // Force to save all player data and worlds
        PrintUtil.msgInfo(context, "Saving players ...");
        server.getPlayerManager().saveAllPlayerData();
        PrintUtil.msgInfo(context, "Saving worlds ...");
        server.save(true, true, true);

        // Log start time
        startTime = System.currentTimeMillis();
        return true;
    }
}
