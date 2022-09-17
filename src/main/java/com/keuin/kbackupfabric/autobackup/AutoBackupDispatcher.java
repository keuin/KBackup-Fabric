package com.keuin.kbackupfabric.autobackup;

import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class AutoBackupDispatcher {

    private Timer timer = null;
    private final Logger logger = Logger.getLogger("KBackup-AutoBackup");
    private boolean skipIfNoPlayerLoggedIn;
    private final PlayerActivityTracker playerActivityTracker;

    public AutoBackupDispatcher(int intervalSeconds, boolean skipIfNoPlayerLoggedIn, PlayerActivityTracker playerActivityTracker) {
        if (intervalSeconds < 1)
            throw new IllegalArgumentException("interval is too small");
        this.skipIfNoPlayerLoggedIn = skipIfNoPlayerLoggedIn;
        this.playerActivityTracker = playerActivityTracker;
        // start timer
        Optional.ofNullable(timer).ifPresent(Timer::cancel);
        Timer timer = new Timer("AutoBackupTimer");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (playerActivityTracker.getCheckpoint() || !skipIfNoPlayerLoggedIn) {
                    logger.info("Making regular backup...");
                    // TODO: perform a backup
                }
            }
        }, 0L, intervalSeconds * 1000L);
        this.timer = timer;
    }

    public void setSkipIfNoPlayerLoggedIn(boolean skipIfNoPlayerLoggedIn) {
        this.skipIfNoPlayerLoggedIn = skipIfNoPlayerLoggedIn;
    }

    public synchronized void stop() {
        timer.cancel();
        timer = null;
    }
}
