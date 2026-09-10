package com.carloshdzz22.zombieinfection.config;

/** A bounded immutable infection roll, expressed in percentage points (0..100). */
public record InfectionProfile(double chance, int minimum, int maximum) {
    public InfectionProfile {
        if (!Double.isFinite(chance)) throw new IllegalArgumentException("Infection chance must be finite");
        chance = Math.clamp(chance, 0, 1);
        int low = Math.clamp(Math.min(minimum, maximum), 0, 100);
        maximum = Math.clamp(Math.max(minimum, maximum), 0, 100);
        minimum = low;
    }
}
