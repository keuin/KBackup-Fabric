package com.keuin.kbackupfabric;

import com.keuin.kbackupfabric.backup.BackupFilesystemUtil;
import com.keuin.kbackupfabric.backup.suggestion.BackupNameSuggestionProvider;
import com.keuin.kbackupfabric.event.OnPlayerConnect;
import com.keuin.kbackupfabric.metadata.BackupMetadata;
import com.keuin.kbackupfabric.metadata.MetadataHolder;
import com.keuin.kbackupfabric.notification.DistinctNotifiable;
import com.keuin.kbackupfabric.notification.NotificationManager;
import com.keuin.kbackupfabric.ui.KBCommands;
import com.keuin.kbackupfabric.util.DateUtil;
import com.keuin.kbackupfabric.util.PrintUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import static org.apache.commons.io.FileUtils.forceDelete;

/**
 * This is the Main file of this plugin.
 * It contains all events, including the init event.
 */
public final class KBPluginEvents implements ModInitializer {

    //private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        System.out.println("Binding events and commands ...");

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            KBCommandsRegister.registerCommands(dispatcher);
        });
        ServerLifecycleEvents.SERVER_STARTED.register(
                this::onStartServer
        );
    }

    public void onStartServer(MinecraftServer server) {

        // Buggy: this does not work
        if (!(server instanceof MinecraftDedicatedServer))
            throw new RuntimeException("KBackup is a server-side-only plugin. Please do not use it in client-side.");

        // Bind fabric events
        OnPlayerConnect.ON_PLAYER_CONNECT.register((connection, player)
                -> NotificationManager.INSTANCE.notifyPlayer(DistinctNotifiable.fromServerPlayerEntity(player)));

        // Initialize player manager reference
        PrintUtil.setPlayerManager(server.getPlayerManager());

        // Initialize backup manager server reference
        KBCommands.setServer(server);

        // Update backup suggestion list
        BackupNameSuggestionProvider.setBackupSaveDirectory(BackupFilesystemUtil.getBackupSaveDirectory(server).getPath());

        // Check if we have just recovered from a previous backup. If so, print message.
        try {
            File levelDirectory = new File(server.getRunDirectory(), ((MinecraftDedicatedServer) server).getLevelName());
            File metadataFile = new File(levelDirectory, BackupMetadata.metadataFileName);
            if (metadataFile.exists()) {
                // Metadata exists. Deserialize.
                BackupMetadata metadata;
                FileInputStream fileInputStream = new FileInputStream(metadataFile);
                ObjectInputStream in = new ObjectInputStream(fileInputStream);
                metadata = (BackupMetadata) in.readObject();
                in.close();
                fileInputStream.close();

                // Print metadata
                MetadataHolder.setMetadata(metadata);
                PrintUtil.info("Restored world from a previous backup:");
                PrintUtil.info("Backup Name: " + metadata.getBackupName());
                PrintUtil.info("Create Time: " + DateUtil.fromEpochMillis(metadata.getBackupTime()));

                // Delete metadata file
                if (!metadataFile.delete()) {
                    System.gc();
                    forceDelete(metadataFile);
                }
            }
        } catch (IOException | ClassNotFoundException ignored) {
        }

        PrintUtil.info("KBackup is a free software. Project home: https://github.com/keuin/KBackup-Fabric");
    }
}
