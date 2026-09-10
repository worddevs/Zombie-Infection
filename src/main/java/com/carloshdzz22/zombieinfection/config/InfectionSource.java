package com.carloshdzz22.zombieinfection.config;

/** Defaults taken from the existing melee, projectile and cloud implementations. */
public enum InfectionSource {
    COMMON("common", 0.20, 5, 10),
    RUNNER("runner", 0.30, 8, 15),
    BLOATER("bloater", 0.40, 10, 18),
    // Spitter AI uses ranged attacks; its inherited melee previously caused no infection.
    SPITTER("spitter", 0, 0, 0),
    SPITTER_PROJECTILE("spitterProjectile", 0.25, 5, 10),
    SPITTER_ZONE("spitterZone", 0.10, 1, 3),
    BLOATER_CLOUD("bloaterCloud", 0.15, 2, 5);

    private final String key;
    private final InfectionProfile defaults;

    InfectionSource(String key, double chance, int minimum, int maximum) {
        this.key = key;
        this.defaults = new InfectionProfile(chance, minimum, maximum);
    }

    public String key() { return key; }
    public String prefix() { return "infection." + key + "."; }
    public InfectionProfile defaults() { return defaults; }
}
