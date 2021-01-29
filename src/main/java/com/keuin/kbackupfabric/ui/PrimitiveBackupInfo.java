package com.keuin.kbackupfabric.ui;

import com.keuin.kbackupfabric.backup.name.BackupFileNameEncoder;
import com.keuin.kbackupfabric.backup.name.PrimitiveBackupFileNameEncoder;
import com.keuin.kbackupfabric.operation.backup.method.ConfiguredBackupMethod;
import com.keuin.kbackupfabric.util.FilesystemUtil;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.time.LocalDateTime;

/**
 * Used in UI part.
 */
public class PrimitiveBackupInfo implements BackupInfo {
    private final String name;
    private final LocalDateTime creationTime;
    private final long sizeBytes;
    private final String fileName;

    @Deprecated
    private PrimitiveBackupInfo(String name, LocalDateTime creationTime, long sizeBytes) {
        this.name = name;
        this.creationTime = creationTime;
        this.sizeBytes = sizeBytes;
        this.fileName = PrimitiveBackupFileNameEncoder.INSTANCE.encode(name, creationTime);
    }

    private PrimitiveBackupInfo(String fileName, long sizeBytes) {
        this.fileName = fileName;
        BackupFileNameEncoder.BackupBasicInformation info = PrimitiveBackupFileNameEncoder.INSTANCE.decode(fileName);
        if (info == null)
            throw new IllegalArgumentException("Invalid file name.");
        this.name = info.customName;
        this.creationTime = info.time;
        this.sizeBytes = sizeBytes;
    }

    public static PrimitiveBackupInfo fromFile(File zipFile) {
        // TODO: fix this, use metadata file instead
//        fileName = zipFile.getName();
//        BackupFileNameEncoder.BackupBasicInformation info = PrimitiveBackupFileNameEncoder.INSTANCE.decode(fileName);
//        if (info == null)
//            throw new IllegalArgumentException("Invalid file name.");
//        return new PrimitiveBackupInfo(info.customName, info.time, FilesystemUtil.getFileSizeBytes(zipFile));
        return new PrimitiveBackupInfo(zipFile.getName(), FilesystemUtil.getFileSizeBytes(zipFile));
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
        return "ZIP";
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
