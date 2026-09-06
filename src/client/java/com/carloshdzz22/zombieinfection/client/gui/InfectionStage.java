package com.carloshdzz22.zombieinfection.client.gui;

/**
 * Presentation-only view of the infection value synchronized by the server.
 */
public enum InfectionStage {
    HEALTHY(0, "healthy", 0xFF64D6A4, new String[] {"none"}),
    EXPOSED(1, "exposed", 0xFFD9C86C, new String[] {"exposure"}),
    EARLY(2, "early", 0xFFF1C84B, new String[] {"hunger", "mild_weakness"}),
    MODERATE(3, "moderate", 0xFFF08A3C, new String[] {"weakness", "hunger", "nausea"}),
    SEVERE(4, "severe", 0xFFE34B4B,
            new String[] {"severe_weakness", "hunger", "reduced_mobility", "advanced"});

    private final int rank;
    private final String translationSuffix;
    private final int color;
    private final String[] symptomSuffixes;

    InfectionStage(int rank, String translationSuffix, int color, String[] symptomSuffixes) {
        this.rank = rank;
        this.translationSuffix = translationSuffix;
        this.color = color;
        this.symptomSuffixes = symptomSuffixes;
    }

    public static InfectionStage fromValue(int infection) {
        if (infection >= 75) {
            return SEVERE;
        }
        if (infection >= 50) {
            return MODERATE;
        }
        if (infection >= 25) {
            return EARLY;
        }
        if (infection >= 1) {
            return EXPOSED;
        }
        return HEALTHY;
    }

    public int rank() {
        return rank;
    }

    public int color() {
        return color;
    }

    public String stageKey() {
        return "screen.zombie-infection.status.stage." + translationSuffix;
    }

    public String treatmentKey() {
        return "screen.zombie-infection.status.treatment." + translationSuffix;
    }

    public String[] symptomKeys() {
        String[] keys = new String[symptomSuffixes.length];
        for (int index = 0; index < symptomSuffixes.length; index++) {
            keys[index] = "screen.zombie-infection.status.symptom." + symptomSuffixes[index];
        }
        return keys;
    }
}
