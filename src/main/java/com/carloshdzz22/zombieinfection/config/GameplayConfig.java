package com.carloshdzz22.zombieinfection.config;

import com.carloshdzz22.zombieinfection.ZombieInfection;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;

public final class GameplayConfig {
    public static final SettingsFile<GameplaySettings> FILE = new SettingsFile<>(
            FabricLoader.getInstance().getConfigDir().resolve("zombie-infection-gameplay.properties"),
            GameplaySettings::parse);
    public static final String TEMPLATE = """
            # Zombie Infection: server-authoritative gameplay settings.
            # Edit this file, then use /infection config reload (operator level 2).
            # Invalid reloads keep the previous settings. Missing keys use defaults.
            # Disabling natural spawning does not remove existing mobs or block eggs/summon.
            naturalSpawning=true
            # Overall acceptance multiplier: 0..2. Acceptance is capped at 1.
            densityMultiplier=1.0
            # Day (ticks 0..11999): 0..1 each. Night (12000..23999): 0..1.5.
            dayCommonMultiplier=0.55
            dayRunnerMultiplier=0.35
            daySpecialMultiplier=0.20
            nightMultiplier=1.0
            # Scale Outbreak caps, rounded up: total 0.25..2, specials 0..2.
            # Absolute safety limits: 80 total / 14 specials. Specials=0 disables their natural spawn.
            totalCapMultiplier=1.0
            specialCapMultiplier=1.0
            # Player distance 24..64; artificial block-light limit 0..15 (sunlight is independent).
            minimumPlayerDistance=24
            maximumBlockLight=7
            """ + infectionTemplate();

    private static String infectionTemplate() {
        var text = new StringBuilder("\n# Infection: chance 0..1, min/max integer points 0..100.\n"
                + "# Finite out-of-range infection values are clamped; reversed min/max are sorted.\n"
                + "# Invalid numbers, NaN, infinity and unknown keys reject reload atomically.\n"
                + "# Spitter melee is disabled by default (0/0/0); its AI remains ranged.\n");
        for (var source : InfectionSource.values()) {
            var defaults = source.defaults();
            text.append(source.prefix()).append("chance=").append(defaults.chance()).append('\n');
            text.append(source.prefix()).append("min=").append(defaults.minimum()).append('\n');
            text.append(source.prefix()).append("max=").append(defaults.maximum()).append('\n');
        }
        return text.toString();
    }

    private GameplayConfig() { }

    public static void initialize() {
        try {
            FILE.initialize(TEMPLATE);
            FILE.addMissingDefaults(TEMPLATE);
        } catch (IOException | IllegalArgumentException error) {
            ZombieInfection.LOGGER.error("Could not load {}; retaining valid gameplay settings: {}",
                    FILE.path(), error.getMessage());
        }
    }

    public static GameplaySettings current() {
        return FILE.current();
    }
}
