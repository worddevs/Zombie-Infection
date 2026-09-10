package com.carloshdzz22.zombieinfection.config;

import java.util.Properties;
import java.util.Set;

public record GameplaySettings(boolean naturalSpawning, double densityMultiplier,
        double dayCommonMultiplier, double dayRunnerMultiplier, double daySpecialMultiplier,
        double nightMultiplier, double totalCapMultiplier, double specialCapMultiplier,
        int minimumPlayerDistance, int maximumBlockLight,
        java.util.Map<InfectionSource, InfectionProfile> infections) {
    public InfectionProfile infection(InfectionSource source) { return infections.get(source); }

    public static GameplaySettings parse(Properties properties) {
        var values = new ConfigValues(properties);
        var keys = new java.util.HashSet<>(Set.of("naturalSpawning", "densityMultiplier", "dayCommonMultiplier",
                "dayRunnerMultiplier", "daySpecialMultiplier", "nightMultiplier", "totalCapMultiplier",
                "specialCapMultiplier", "minimumPlayerDistance", "maximumBlockLight"));
        var infections = new java.util.EnumMap<InfectionSource, InfectionProfile>(InfectionSource.class);
        for (var source : InfectionSource.values()) {
            String prefix = source.prefix();
            keys.addAll(Set.of(prefix + "chance", prefix + "min", prefix + "max"));
            var defaults = source.defaults();
            infections.put(source, new InfectionProfile(
                    values.clampedNumber(prefix + "chance", defaults.chance(), 0, 1),
                    values.clampedInteger(prefix + "min", defaults.minimum(), 0, 100),
                    values.clampedInteger(prefix + "max", defaults.maximum(), 0, 100)));
        }
        values.requireKnownKeys(keys);
        return new GameplaySettings(values.bool("naturalSpawning", true),
                values.number("densityMultiplier", 1, 0, 2),
                values.number("dayCommonMultiplier", 0.55, 0, 1),
                values.number("dayRunnerMultiplier", 0.35, 0, 1),
                values.number("daySpecialMultiplier", 0.20, 0, 1),
                values.number("nightMultiplier", 1, 0, 1.5),
                values.number("totalCapMultiplier", 1, 0.25, 2),
                values.number("specialCapMultiplier", 1, 0, 2),
                values.integer("minimumPlayerDistance", 24, 24, 64),
                values.integer("maximumBlockLight", 7, 0, 15), java.util.Map.copyOf(infections));
    }

    public int totalCap(int base) {
        return Math.max(1, Math.min(80, (int) Math.ceil(base * totalCapMultiplier)));
    }

    public int specialCap(int base, int totalCap) {
        return Math.min(totalCap, Math.min(14, (int) Math.ceil(base * specialCapMultiplier)));
    }
}
