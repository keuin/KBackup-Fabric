package com.keuin.kbackupfabric.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.world.World;

import java.io.File;

/**
 * Functions deal with file name, directory name about Minecraft saves.
 */
public final class BackupFilesystemUtil {

    private static final String backupSaveDirectoryName = "backups";
    private static final String backupFileNamePrefix = "kbackup-";

    public static String getBackupFileName(String backupName) {
        return backupFileNamePrefix + backupName + ".zip";
    }

    public static String getBackupName(String backupFileName) {
        try {
            if (backupFileName.matches(backupFileNamePrefix + ".+\\.zip"))
                return backupFileName.substring(backupFileNamePrefix.length(), backupFileName.length() - 4);
        } catch (IndexOutOfBoundsException ignored) {
        }
        return backupFileName;
    }

    public static boolean isBackupNameValid(String backupName, MinecraftServer server) {
        File backupFile = new File(getBackupSaveDirectory(server), getBackupFileName(backupName));
        return backupFile.isFile();
    }

    public static File getBackupSaveDirectory(MinecraftServer server) {
        return new File(server.getRunDirectory(), backupSaveDirectoryName);
    }

    public static String getLevelPath(MinecraftServer server) {
        return (new File(server.getRunDirectory(), server.getLevelName())).getAbsolutePath();
    }

    public static String getWorldDirectoryName(World world) throws NoSuchFieldException, IllegalAccessException {
        File saveDir;
        ThreadedAnvilChunkStorage threadedAnvilChunkStorage = (ThreadedAnvilChunkStorage) ReflectionUtils.getPrivateField(world.getChunkManager(), "threadedAnvilChunkStorage");
        saveDir = (File) ReflectionUtils.getPrivateField(threadedAnvilChunkStorage, "saveDir");
        return saveDir.getName();
    }

    public static String getBackupFileNamePrefix() {
        return backupFileNamePrefix;
    }
}
