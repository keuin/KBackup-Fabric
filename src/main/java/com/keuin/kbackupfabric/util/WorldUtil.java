package com.keuin.kbackupfabric.util;

import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.world.World;

import java.io.File;

public class WorldUtil {
    public static String getWorldDirectoryName(World world) throws NoSuchFieldException, IllegalAccessException {
        File saveDir;
        ThreadedAnvilChunkStorage threadedAnvilChunkStorage = (ThreadedAnvilChunkStorage) ReflectionUtils.getPrivateField(world.getChunkManager(), "threadedAnvilChunkStorage");
        saveDir = (File) ReflectionUtils.getPrivateField(threadedAnvilChunkStorage, "saveDir");
        return saveDir.getName();
    }
}
