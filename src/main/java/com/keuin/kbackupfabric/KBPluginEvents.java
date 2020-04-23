package com.keuin.kbackupfabric;

import com.keuin.kbackupfabric.data.BackupMetadata;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.server.ServerStartCallback;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.keuin.kbackupfabric.util.PrintUtil.debug;
import static com.keuin.kbackupfabric.util.PrintUtil.info;
import static org.apache.commons.io.FileUtils.forceDelete;

/**
 * This is the Main file of this plugin.
 * It contains all events, including the init event.
 */
public final class KBPluginEvents implements ModInitializer, ServerStartCallback {
    @Override
    public void onInitialize() {
        System.out.println("Initializing KBackup...");
        CommandRegistry.INSTANCE.register(false, KBRegister::registerCommands);
        ServerStartCallback.EVENT.register(this);
    }

    @Override
    public void onStartServer(MinecraftServer server) {
        // When the server starts, we check if we have just recovered from a backup.
        // If so, then we print some message.
        debug("KBackup onStartServer");
        // TODO:
        // Check
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
                info("Recovered from previous backup:");
                info("Backup Name: " + metadata.getBackupName());
                info("Create Time: " + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date(metadata.getBackupTime())));

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
