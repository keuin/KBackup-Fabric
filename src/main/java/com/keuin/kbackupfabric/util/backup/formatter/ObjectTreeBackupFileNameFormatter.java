package com.keuin.kbackupfabric.util.backup.formatter;

import com.keuin.kbackupfabric.util.backup.BackupFilesystemUtil;
import com.keuin.kbackupfabric.util.backup.BackupNameTimeFormatter;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import org.spongepowered.asm.mixin.Overwrite;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ObjectTreeBackupFileNameFormatter implements BackupFileNameFormatter {

    private static final ObjectTreeBackupFileNameFormatter instance = new ObjectTreeBackupFileNameFormatter();

    public static ObjectTreeBackupFileNameFormatter getInstance() {
        return instance;
    }

    @Override
    public BackupFileNameFormatter.BackupFileName format(@NotNull String fileName) {
        LocalDateTime time = getTime(fileName);
        String name = getBackupName(fileName);
        return new BackupFileNameFormatter.BackupFileName(time,name);
    }

    @Nullable
    private LocalDateTime getTime(String fileName) {
        Matcher matcher = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}_[0-9]{2}-[0-9]{2}-[0-9]{2}").matcher(fileName);
        if (matcher.find()) {
            String timeString = matcher.group(0);
            return BackupNameTimeFormatter.timeStringToLocalDateTime(timeString);
        }
        return null;
    }

    private String getBackupName(String backupFileName) {
        try {
            if (backupFileName.matches(BackupFilesystemUtil.getBackupFileNamePrefix() + ".+\\.json"))
                return backupFileName.substring(BackupFilesystemUtil.getBackupFileNamePrefix().length(), backupFileName.length() - 4);
        } catch (IndexOutOfBoundsException ignored) {
        }
        return backupFileName;
    }

}
