package com.carloshdzz22.zombieinfection.outbreak;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * World-wide epidemic progression derived from the Overworld day count.
 * No redundant saved value is required: time persistence is already handled by Minecraft.
 */
public enum OutbreakLevel {
    CONTAINED(0, 0, 92, 5, 2, 1, 0.22, 12, 1),
    EMERGING(1, 3, 86, 8, 3, 3, 0.32, 18, 2),
    SPREADING(2, 7, 78, 12, 5, 5, 0.46, 24, 3),
    SEVERE(3, 15, 70, 16, 7, 7, 0.62, 32, 5),
    CRITICAL(4, 30, 62, 20, 9, 9, 0.78, 40, 7);

    public static final long TICKS_PER_DAY = 24_000L;

    private final int index;
    private final long firstDay;
    private final int commonWeight;
    private final int runnerWeight;
    private final int bloaterWeight;
    private final int spitterWeight;
    private final double densityMultiplier;
    private final int localPopulationCap;
    private final int localSpecialCap;

    OutbreakLevel(int index, long firstDay, int commonWeight, int runnerWeight, int bloaterWeight,
            int spitterWeight, double densityMultiplier, int localPopulationCap, int localSpecialCap) {
        this.index = index;
        this.firstDay = firstDay;
        this.commonWeight = commonWeight;
        this.runnerWeight = runnerWeight;
        this.bloaterWeight = bloaterWeight;
        this.spitterWeight = spitterWeight;
        this.densityMultiplier = densityMultiplier;
        this.localPopulationCap = localPopulationCap;
        this.localSpecialCap = localSpecialCap;
    }

    public static OutbreakLevel fromDay(long day) {
        if (day >= CRITICAL.firstDay) {
            return CRITICAL;
        }
        if (day >= SEVERE.firstDay) {
            return SEVERE;
        }
        if (day >= SPREADING.firstDay) {
            return SPREADING;
        }
        if (day >= EMERGING.firstDay) {
            return EMERGING;
        }
        return CONTAINED;
    }

    public static OutbreakLevel from(ServerLevel level) {
        ServerLevel overworld = level.getServer().getLevel(Level.OVERWORLD);
        return fromDay(day(overworld != null ? overworld.getDayTime() : level.getDayTime()));
    }

    public static long day(long dayTime) {
        return Math.max(0L, dayTime / TICKS_PER_DAY);
    }

    public int index() {
        return index;
    }

    public long firstDay() {
        return firstDay;
    }

    public int runnerWeight() {
        return runnerWeight;
    }

    public int commonWeight() {
        return commonWeight;
    }

    public int bloaterWeight() {
        return bloaterWeight;
    }

    public int spitterWeight() {
        return spitterWeight;
    }

    public double densityMultiplier() {
        return densityMultiplier;
    }

    public int localPopulationCap() {
        return localPopulationCap;
    }

    public int localSpecialCap() {
        return localSpecialCap;
    }

    public String nameKey() {
        return "outbreak.zombie-infection.level." + index + ".name";
    }

    public String descriptionKey() {
        return "outbreak.zombie-infection.level." + index + ".description";
    }
}
