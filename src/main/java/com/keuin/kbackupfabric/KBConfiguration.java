package com.keuin.kbackupfabric;

/**
 * Global plugin configuration.
 */
public class KBConfiguration {
    // auto backup interval in seconds. Set this to a negative value to disable auto backup.
    private final int autoBackupIntervalSeconds;
    // name of backup created automatically. By default, it is `auto-backup`
    private final String autoBackupName;
    // if no player has logged in since previous backup, we skip this backup
    private final boolean skipAutoBackupIfNoPlayerLoggedIn;

    public KBConfiguration() {
        autoBackupIntervalSeconds = -1; // disabled by default
        autoBackupName = "auto-backup";
        skipAutoBackupIfNoPlayerLoggedIn = false;
    }

    public KBConfiguration(int autoBackupIntervalSeconds, String autoBackupName, boolean skipAutoBackupIfNoPlayerLoggedIn) {
        this.autoBackupIntervalSeconds = autoBackupIntervalSeconds;
        this.autoBackupName = autoBackupName;
        this.skipAutoBackupIfNoPlayerLoggedIn = skipAutoBackupIfNoPlayerLoggedIn;
    }

    public int getAutoBackupIntervalSeconds() {
        return autoBackupIntervalSeconds;
    }

    public String getAutoBackupName() {
        return autoBackupName;
    }
}
