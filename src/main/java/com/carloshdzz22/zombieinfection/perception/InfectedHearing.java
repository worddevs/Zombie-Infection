package com.carloshdzz22.zombieinfection.perception;

/** Hearing profiles also bound the local noise query; no entity scan is needed to find its radius. */
public enum InfectedHearing {
    COMMON(26.0, 0.90),
    RUNNER(46.0, 1.25),
    BLOATER(28.0, 0.80),
    SPITTER(34.0, 1.0);

    private static final InfectedHearing[] PROFILES = values();
    private final double range;
    private final double sensitivity;

    InfectedHearing(double range, double sensitivity) {
        this.range = range;
        this.sensitivity = sensitivity;
    }

    public double range() {
        return range;
    }

    public double sensitivity() {
        return sensitivity;
    }

    public static double searchRadius(double eventRadius) {
        double maximum = 0.0;
        for (InfectedHearing profile : PROFILES) {
            maximum = Math.max(maximum, Math.min(profile.range, eventRadius * profile.sensitivity));
        }
        return maximum;
    }
}
