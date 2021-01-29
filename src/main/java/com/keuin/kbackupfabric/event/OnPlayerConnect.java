package com.keuin.kbackupfabric.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerPlayerEntity;

public class OnPlayerConnect {
    public interface PlayerConnectEventCallback {
        void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player);
    }

    public static final Event<PlayerConnectEventCallback> ON_PLAYER_CONNECT = EventFactory.createArrayBacked(PlayerConnectEventCallback.class, callbacks -> (conn, player) -> {
        for (PlayerConnectEventCallback callback : callbacks) {
            callback.onPlayerConnect(conn, player);
        }
    });
}
