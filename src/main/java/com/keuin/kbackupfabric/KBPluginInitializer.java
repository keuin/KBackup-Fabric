package com.keuin.kbackupfabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.CommandRegistry;

public class KBPluginInitializer implements ModInitializer {
    @Override
    public void onInitialize() {
        System.out.println("Initializing KBackup...");
        CommandRegistry.INSTANCE.register(false, KBCommandRegister::register);
    }
}
