package com.keuin.kbackupfabric;

import com.keuin.kbackupfabric.backup.BackupFilesystemUtil;
import com.keuin.kbackupfabric.backup.suggestion.BackupNameSuggestionProvider;
import com.keuin.kbackupfabric.metadata.BackupMetadata;
import com.keuin.kbackupfabric.metadata.MetadataHolder;
import com.keuin.kbackupfabric.util.PrintUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.server.ServerStartCallback;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.apache.commons.io.FileUtils.forceDelete;

/**
 * This is the Main file of this plugin.
 * It contains all events, including the init event.
 */
public final class KBPluginEvents implements ModInitializer, ServerStartCallback {

    //private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        System.out.println("Binding events and commands ...");
        CommandRegistry.INSTANCE.register(false, KBCommandsRegister::registerCommands);
        ServerStartCallback.EVENT.register(this);
    }

    @Override
    public void onStartServer(MinecraftServer server) {

        if (!(server instanceof MinecraftDedicatedServer))
            throw new RuntimeException("KBackup is a server-side-only plugin. Please do not use it in Minecraft client.");

        // Initialize player manager reference
        PrintUtil.setPlayerManager(server.getPlayerManager());

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
                PrintUtil.info("Create Time: " + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date(metadata.getBackupTime())));

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
