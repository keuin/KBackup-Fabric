package com.keuin.kbackupfabric.util.backup.provider;

import com.keuin.kbackupfabric.util.backup.name.BackupFileNameEncoder;

import java.time.LocalDateTime;

public class IncrementalBackupInformation extends BackupFileNameEncoder.BackupBasicInformation {
    // TODO: show total size for incremental backup

    public IncrementalBackupInformation(String customName, LocalDateTime time) {
        super(customName, time);
    }
}
