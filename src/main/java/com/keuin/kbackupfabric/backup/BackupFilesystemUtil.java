package com.keuin.kbackupfabric.backup;

import com.keuin.kbackupfabric.util.ReflectionUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.world.World;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Functions deal with file name, directory name about Minecraft saves.
 */
public final class BackupFilesystemUtil {

    private static final String BACKUP_SAVE_DIRECTORY_NAME = "backups";
    private static final String INCREMENTAL_BASE_DIRECTORY_NAME = "incremental";
    private static final String backupFileNamePrefix = "kbackup-";

    @Deprecated
    public static String getBackupFileNamePrefix() {
        return backupFileNamePrefix;
    }


//    @Deprecated
//    public static String getBackupName(String backupFileName) {
//        try {
//            if (backupFileName.matches(backupFileNamePrefix + ".+\\.zip"))
//                return backupFileName.substring(backupFileNamePrefix.length(), backupFileName.length() - 4);
//        } catch (IndexOutOfBoundsException ignored) {
//        }
//        return backupFileName;
//    }

    public static boolean isBackupFileExists(String backupFileName, MinecraftServer server) {
        File backupFile = new File(getBackupSaveDirectory(server), backupFileName);
        return backupFile.isFile();
    }

    public static File getBackupSaveDirectory(MinecraftServer server) {
        return new File(server.getRunDirectory(), BACKUP_SAVE_DIRECTORY_NAME);
    }

    public static File getIncrementalBackupBaseDirectory(MinecraftServer server) {
        return new File(server.getRunDirectory(), INCREMENTAL_BASE_DIRECTORY_NAME);
    }

    public static String getLevelPath(MinecraftServer server) throws IOException {
        if (!(server instanceof MinecraftDedicatedServer))
            throw new IllegalStateException("This plugin is server-side only.");
        String path = (new File(server.getRunDirectory().getCanonicalPath(), ((MinecraftDedicatedServer) server).getLevelName())).getAbsolutePath();
        Logger.getLogger("getLevelPath").info(String.format("Level path: %s", path));
        assert (new File(path)).exists();
        return path;
    }

    public static String getWorldDirectoryName(World world) throws NoSuchFieldException, IllegalAccessException {
        File saveDir;
        ThreadedAnvilChunkStorage threadedAnvilChunkStorage = (ThreadedAnvilChunkStorage) ReflectionUtils.getPrivateField(world.getChunkManager(), "threadedAnvilChunkStorage");
        saveDir = (File) ReflectionUtils.getPrivateField(threadedAnvilChunkStorage, "saveDir");
        return saveDir.getName();
    }

    @Deprecated
    public static long getBackupTimeFromBackupFileName(String backupFileName) {
        Matcher matcher = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}_[0-9]{2}-[0-9]{2}-[0-9]{2}").matcher(backupFileName);
        if (matcher.find()) {
            String timeString = matcher.group(0);
            long timeStamp = BackupNameTimeFormatter.timeStringToEpochSeconds(timeString);
            System.out.println(backupFileName + " -> " + timeStamp);
            return timeStamp;
        } else {
            System.err.println("Failed to extract time from " + backupFileName);
        }
        return -1;
    }

    public static String getFriendlyFileSizeString(long sizeBytes) {
        double fileSize = sizeBytes * 1.0 / 1024 / 1024; // Default unit is MB
        if (fileSize > 1000)
            //msgInfo(context, String.format("File size: %.2fGB", fileSize / 1024));
            return String.format("%.2fGB", fileSize / 1024);
        else
            //msgInfo(context, String.format("File size: %.2fMB", fileSize));
            return String.format("%.2fMB", fileSize);
    }
}
