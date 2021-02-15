package com.keuin.kbackupfabric.event.handler;

import com.keuin.kbackupfabric.autobackup.PlayerActivityTracker;
import com.keuin.kbackupfabric.event.OnPlayerConnect;
import com.keuin.kbackupfabric.notification.DistinctNotifiable;
import com.keuin.kbackupfabric.notification.NotificationManager;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerPlayerEntity;

public class ConcretePlayerConnectEventHandler implements OnPlayerConnect.PlayerConnectEventCallback {
    private final PlayerActivityTracker playerActivityTracker;

    public ConcretePlayerConnectEventHandler(PlayerActivityTracker playerActivityTracker) {
        this.playerActivityTracker = playerActivityTracker;
    }

    @Override
    public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player) {
        playerActivityTracker.setCheckpoint();
        NotificationManager.INSTANCE.notifyPlayer(DistinctNotifiable.fromServerPlayerEntity(player));
    }
}
