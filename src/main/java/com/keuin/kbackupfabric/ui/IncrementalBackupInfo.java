package com.keuin.kbackupfabric.ui;

import com.keuin.kbackupfabric.backup.incremental.serializer.IncBackupInfoSerializer;
import com.keuin.kbackupfabric.backup.incremental.serializer.SavedIncrementalBackup;
import com.keuin.kbackupfabric.operation.backup.method.ConfiguredBackupMethod;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Used in UI part.
 */
public class IncrementalBackupInfo implements BackupInfo {
    private final String name;
    private final LocalDateTime creationTime;
    private final long sizeBytes;
    private final String fileName;

    private IncrementalBackupInfo(String name, LocalDateTime creationTime, long sizeBytes, String fileName) {
        this.name = name;
        this.creationTime = creationTime;
        this.sizeBytes = sizeBytes;
        this.fileName = fileName;
    }

    public static IncrementalBackupInfo fromFile(File indexFile) throws IOException {
        SavedIncrementalBackup info = IncBackupInfoSerializer.fromFile(indexFile);
        return new IncrementalBackupInfo(
                info.getBackupName(),
                info.getBackupTime().withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime(),
                info.getTotalSizeBytes(),
                indexFile.getName()
        );
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    @Override
    public long getSizeBytes() {
        return sizeBytes;
    }

    @Override
    public String getType() {
        return "Incremental";
    }

    @Override
    public ConfiguredBackupMethod createConfiguredBackupMethod(MinecraftServer server) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String getBackupFileName() {
        return fileName;
    }
}
