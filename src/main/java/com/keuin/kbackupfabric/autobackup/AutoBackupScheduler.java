package com.keuin.kbackupfabric.autobackup;

import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class AutoBackupScheduler {

    private Timer timer = null;
    private final Logger logger = Logger.getLogger(AutoBackupScheduler.class.getName());
    private boolean skipIfNoPlayerLoggedIn;
    private final PlayerActivityTracker playerActivityTracker;

    public AutoBackupScheduler(int intervalSeconds, boolean skipIfNoPlayerLoggedIn, PlayerActivityTracker playerActivityTracker) {
        this.skipIfNoPlayerLoggedIn = skipIfNoPlayerLoggedIn;
        this.playerActivityTracker = playerActivityTracker;
        if (intervalSeconds > 0)
            setInterval(intervalSeconds);
    }

    public synchronized void setInterval(int intervalSeconds) {
        Optional.ofNullable(timer).ifPresent(Timer::cancel);
        Timer newTimer = new Timer("AutoBackupTimer");
        newTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                toggleBackup();
            }
        }, 0L, intervalSeconds * 1000L);
        timer = newTimer;
    }

    public void setSkipIfNoPlayerLoggedIn(boolean skipIfNoPlayerLoggedIn) {
        this.skipIfNoPlayerLoggedIn = skipIfNoPlayerLoggedIn;
    }

    public synchronized void stop() {
        timer.cancel();
        timer = null;
    }

    private void toggleBackup() {
        if (playerActivityTracker.getCheckpoint() || !skipIfNoPlayerLoggedIn) {
            logger.info("Interval backup event is triggered.");
            // TODO: perform a backup
        }
    }

}
