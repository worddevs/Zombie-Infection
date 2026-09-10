package com.carloshdzz22.zombieinfection.client.config;

import com.carloshdzz22.zombieinfection.ZombieInfection;
import com.carloshdzz22.zombieinfection.config.SettingsFile;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.network.chat.Component;
import java.io.IOException;

public final class HealthBarConfig {
    public static final SettingsFile<HealthBarSettings> FILE = new SettingsFile<>(
            FabricLoader.getInstance().getConfigDir().resolve("zombie-infection-healthbars.properties"),
            HealthBarSettings::parse);
    private static final String TEMPLATE = """
            # Local visual settings, including when playing on someone else's server.
            # Edit this file, then use /infectionhud reload. No operator permission needed.
            enabled=true
            # Range: 4..16 blocks. Scale: 0.65..1.5 relative to the default compact panel.
            range=16
            scale=1.0
            # Damage visibility: 0..6 seconds; fade: 0.05..1 second.
            damageSeconds=3.0
            fadeSeconds=0.25
            damageFlash=true
            damageTrail=true
            """;

    private HealthBarConfig() { }

    public static void initialize() {
        try {
            FILE.initialize(TEMPLATE);
        } catch (IOException | IllegalArgumentException error) {
            ZombieInfection.LOGGER.error("Could not load {}; retaining valid health-bar settings: {}",
                    FILE.path(), error.getMessage());
        }
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                ClientCommandManager.literal("infectionhud")
                        .then(ClientCommandManager.literal("reload").executes(context -> {
                            try {
                                FILE.reload();
                                context.getSource().sendFeedback(Component.translatable("command.zombie-infection.healthbars.reloaded"));
                                return 1;
                            } catch (IOException | IllegalArgumentException error) {
                                context.getSource().sendError(Component.translatable("command.zombie-infection.config.failed", error.getMessage()));
                                return 0;
                            }
                        }))));
    }

    public static HealthBarSettings current() {
        return FILE.current();
    }
}
