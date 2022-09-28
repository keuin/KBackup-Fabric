package com.keuin.kbackupfabric.notification;

import net.minecraft.network.MessageType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

/**
 * Decouple from ServerPlayerEntity, in case further migration to other APIs.
 */
public interface DistinctNotifiable {

    /**
     * Does the receiver has privilege to receive some special message.
     */
    boolean isPrivileged();

    void notify(Text text);

    /**
     * Get an unique, non-null object that identifies this notifiable instance.
     * The identifier must be immutable and implement their own equals method.
     *
     * @return the identifier.
     */
    Object getIdentifier();

    static DistinctNotifiable fromServerPlayerEntity(ServerPlayerEntity serverPlayerEntity) {
        return new DistinctNotifiable() {
            @Override
            public boolean isPrivileged() {
                return serverPlayerEntity.server.getPermissionLevel(serverPlayerEntity.getGameProfile()) >= serverPlayerEntity.server.getOpPermissionLevel();
            }

            @Override
            public void notify(Text text) {
                serverPlayerEntity.sendMessage(text, MessageType.SYSTEM, Util.NIL_UUID);
            }

            @Override
            public Object getIdentifier() {
                return serverPlayerEntity.getUuid();
            }
        };
    }
}
