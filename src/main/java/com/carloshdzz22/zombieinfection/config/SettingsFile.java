package com.carloshdzz22.zombieinfection.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.StandardOpenOption;
import java.util.Properties;
import java.util.function.Function;

/** A failed reload never replaces the last valid immutable settings or rewrites user data. */
public final class SettingsFile<T> {
    private final Path path;
    private final Function<Properties, T> parser;
    private volatile T current;

    public SettingsFile(Path path, Function<Properties, T> parser) {
        this.path = path;
        this.parser = parser;
        this.current = parser.apply(new Properties());
    }

    public T current() {
        return current;
    }

    public Path path() {
        return path;
    }

    public synchronized void initialize(String template) throws IOException {
        Files.createDirectories(path.getParent());
        try {
            Files.writeString(path, template, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        } catch (FileAlreadyExistsException ignored) {
            // Existing files, including invalid files, always belong to the user.
        }
        reload();
    }

    public synchronized void reload() throws IOException {
        Properties properties = new Properties();
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        T replacement = parser.apply(properties);
        current = replacement;
    }

    /** Append new defaults after a successful load, preserving existing values and comments. */
    public synchronized void addMissingDefaults(String template) throws IOException {
        Properties existing = new Properties();
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { existing.load(reader); }
        parser.apply(existing);
        var missing = new StringBuilder();
        for (String line : template.lines().toList()) {
            int equals = line.indexOf('=');
            if (line.stripLeading().startsWith("#") || equals <= 0) continue;
            String key = line.substring(0, equals).trim();
            if (!existing.containsKey(key)) missing.append(line).append('\n');
        }
        if (!missing.isEmpty()) {
            Files.writeString(path, "\n# New defaults added by Zombie Infection; existing settings preserved.\n" + missing,
                    StandardCharsets.UTF_8, StandardOpenOption.APPEND);
            reload();
        }
    }
}
