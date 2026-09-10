package com.carloshdzz22.zombieinfection.client.config;

import com.carloshdzz22.zombieinfection.config.ConfigValues;
import java.util.Properties;
import java.util.Set;

public record HealthBarSettings(boolean enabled, int range, double scale, double damageSeconds,
        double fadeSeconds, boolean damageFlash, boolean damageTrail) {
    public static HealthBarSettings parse(Properties properties) {
        var values = new ConfigValues(properties);
        values.requireKnownKeys(Set.of("enabled", "range", "scale", "damageSeconds", "fadeSeconds",
                "damageFlash", "damageTrail"));
        return new HealthBarSettings(values.bool("enabled", true), values.integer("range", 16, 4, 16),
                values.number("scale", 1, 0.65, 1.5), values.number("damageSeconds", 3, 0, 6),
                values.number("fadeSeconds", 0.25, 0.05, 1), values.bool("damageFlash", true),
                values.bool("damageTrail", true));
    }
}
