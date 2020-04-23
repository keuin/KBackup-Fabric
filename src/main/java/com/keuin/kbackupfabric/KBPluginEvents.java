package com.keuin.kbackupfabric;

import com.keuin.kbackupfabric.data.BackupMetadata;
import com.keuin.kbackupfabric.util.BackupFilesystemUtil;
import com.keuin.kbackupfabric.util.BackupNameSuggestionProvider;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.server.ServerStartCallback;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        System.out.println("KBackup: Binding events and commands ...");
        CommandRegistry.INSTANCE.register(false, KBCommandRegister::registerCommands);
        ServerStartCallback.EVENT.register(this);
    }

    @Override
    public void onStartServer(MinecraftServer server) {
        LOGGER.debug("KBackup: Initializing ...");

        // Update backup suggestion list
        BackupNameSuggestionProvider.setBackupSaveDirectory(BackupFilesystemUtil.getBackupSaveDirectory(server).getPath());

        // Check if we have just recovered from a previous backup. If so, print message.
        try {
            File levelDirectory = new File(server.getRunDirectory(), server.getLevelName());
            File metadataFile = new File(levelDirectory, BackupMetadata.metadataFileName);
            if (metadataFile.exists()) {
                // Metadata exists. Deserialize.
                BackupMetadata metadata = null;
                FileInputStream fileInputStream = new FileInputStream(metadataFile);
                ObjectInputStream in = new ObjectInputStream(fileInputStream);
                metadata = (BackupMetadata) in.readObject();
                in.close();
                fileInputStream.close();

                // Print metadata
                LOGGER.info("Recovered from previous backup:");
                LOGGER.info("Backup Name: " + metadata.getBackupName());
                LOGGER.info("Create Time: " + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date(metadata.getBackupTime())));

                // Delete metadata file
                if (!metadataFile.delete()) {
                    System.gc();
                    forceDelete(metadataFile);
                }
            }
        } catch (IOException | ClassNotFoundException ignored) {
        }
    }
}
