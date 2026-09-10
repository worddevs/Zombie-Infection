package com.carloshdzz22.zombieinfection.config;

import java.util.Properties;
import java.util.Set;

/** Strict parsing catches typos, NaN and out-of-range values before a config goes live. */
public record ConfigValues(Properties properties) {
    public void requireKnownKeys(Set<String> keys) {
        for (String key : properties.stringPropertyNames()) {
            if (!keys.contains(key)) {
                throw new IllegalArgumentException("Unknown setting: " + key);
            }
        }
    }

    public double number(String key, double fallback, double min, double max) {
        String raw = properties.getProperty(key, Double.toString(fallback)).trim();
        double value;
        try {
            value = Double.parseDouble(raw);
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException(key + " must be a number", error);
        }
        if (!Double.isFinite(value) || value < min || value > max) {
            throw new IllegalArgumentException(key + " must be between " + min + " and " + max);
        }
        return value;
    }

    public int integer(String key, int fallback, int min, int max) {
        double value = number(key, fallback, min, max);
        if (value != Math.rint(value)) {
            throw new IllegalArgumentException(key + " must be an integer");
        }
        return (int) value;
    }

    public double clampedNumber(String key, double fallback, double min, double max) {
        return Math.clamp(number(key, fallback, -Double.MAX_VALUE, Double.MAX_VALUE), min, max);
    }

    public int clampedInteger(String key, int fallback, int min, int max) {
        double value = number(key, fallback, -Double.MAX_VALUE, Double.MAX_VALUE);
        if (value != Math.rint(value)) throw new IllegalArgumentException(key + " must be an integer");
        return (int) Math.clamp(value, min, max);
    }

    public boolean bool(String key, boolean fallback) {
        String raw = properties.getProperty(key, Boolean.toString(fallback)).trim();
        if (!raw.equalsIgnoreCase("true") && !raw.equalsIgnoreCase("false")) {
            throw new IllegalArgumentException(key + " must be true or false");
        }
        return Boolean.parseBoolean(raw);
    }
}
