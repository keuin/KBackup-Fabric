package com.keuin.kbackupfabric.util.backup.formatter;

import com.keuin.kbackupfabric.util.backup.BackupNameTimeFormatter;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface BackupFileNameFormatter {

    BackupFileName format(String fileName);

    class BackupFileName {
        public final LocalDateTime time;
        public final String name;

        public BackupFileName(LocalDateTime time, String name) {
            this.time = time;
            this.name = name;
        }
    }

    static BackupFileNameFormatter objectTreeBackup() {
        return ObjectTreeBackupFileNameFormatter.getInstance();
    }

    static BackupFileNameFormatter primitiveZipBackup() {
        return PrimitiveZipBackupFileNameFormatter.getInstance();
    }

}
