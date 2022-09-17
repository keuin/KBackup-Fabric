package com.keuin.kbackupfabric.singleton;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Holds {@link MinecraftServer} instance.
 */
public class MinecraftServerInstance {
    private static AtomicReference<MinecraftServer> instance = new AtomicReference<>();

    public static @NotNull MinecraftServer getInstance() {
        return Objects.requireNonNull(instance.get());
    }

    public static void setInstance(@NotNull MinecraftServer instance) {
        Objects.requireNonNull(instance);
        if (!MinecraftServerInstance.instance.compareAndSet(null, instance)) {
            throw new IllegalStateException("instance already exists");
        }
    }
}
