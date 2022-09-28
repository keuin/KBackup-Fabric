package com.keuin.kbackupfabric.notification;

import com.keuin.kbackupfabric.metadata.BackupMetadata;
import com.keuin.kbackupfabric.metadata.MetadataHolder;
import com.keuin.kbackupfabric.util.DateUtil;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashSet;
import java.util.Set;

/**
 * Notify some users when the server has been restored to a backup.
 */
public class NotificationManager {

    public static final NotificationManager INSTANCE = new NotificationManager();

    private final Set<Object> notified = new HashSet<>();

    private NotificationManager() {
    }

    public void notifyPlayer(DistinctNotifiable distinctNotifiable) {
        Object identifier = distinctNotifiable.getIdentifier();
        if (distinctNotifiable.isPrivileged() && !notified.contains(identifier)) {
            notified.add(identifier);
            notify(distinctNotifiable);
        }
    }

    /**
     * Just notify if necessary. It will not update the set.
     */
    private void notify(DistinctNotifiable notifiable) {
        if (MetadataHolder.hasMetadata()) {
            BackupMetadata backup = MetadataHolder.getMetadata();
            notifiable.notify(
                    Text.literal("The world has been restored to backup ")
                            .append(Text.literal("[" + backup.getBackupName() + "]").setStyle(Style.EMPTY.withColor(Formatting.GREEN)))
                            .append(Text.literal(" (created at "))
                            .append(Text.literal("[" + DateUtil.fromEpochMillis(backup.getBackupTime()) + "]").setStyle(Style.EMPTY.withColor(Formatting.GREEN)))
                            .append(Text.literal(")"))
            );
        }
    }

}
