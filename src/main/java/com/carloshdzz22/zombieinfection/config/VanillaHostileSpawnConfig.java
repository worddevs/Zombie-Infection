package com.carloshdzz22.zombieinfection.config;

import com.carloshdzz22.zombieinfection.ZombieInfection;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;

public record VanillaHostileSpawnConfig(
        boolean allowVanillaHostileSpawns,
        boolean spawnVanillaZombies,
        boolean spawnSkeletons,
        boolean spawnCreepers) {
    private static final Set<EntityType<?>> ZOMBIE_FAMILY = Set.of(
            EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER, EntityType.HUSK, EntityType.DROWNED);
    private static final Set<EntityType<?>> SKELETON_FAMILY = Set.of(
            EntityType.SKELETON, EntityType.STRAY, EntityType.BOGGED);
    private static final Path PATH = FabricLoader.getInstance().getConfigDir()
            .resolve("zombie-infection-spawns.properties");
    private static volatile VanillaHostileSpawnConfig current = defaults();

    public static VanillaHostileSpawnConfig load() {
        Properties properties = new Properties();
        if (Files.exists(PATH)) {
            try (var reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
                properties.load(reader);
            } catch (IOException exception) {
                ZombieInfection.LOGGER.error("Could not read {}", PATH, exception);
            }
        } else {
            writeDefaults();
        }
        VanillaHostileSpawnConfig config = new VanillaHostileSpawnConfig(
                read(properties, "allowVanillaHostileSpawns", false),
                read(properties, "spawnVanillaZombies", false),
                read(properties, "spawnSkeletons", false),
                read(properties, "spawnCreepers", false));
        current = config;
        ZombieInfection.LOGGER.info(
                "Vanilla Overworld hostile spawning: master={}, zombies={}, skeletons={}, creepers={}",
                config.allowVanillaHostileSpawns, config.spawnVanillaZombies,
                config.spawnSkeletons, config.spawnCreepers);
        return config;
    }

    public static VanillaHostileSpawnConfig current() {
        return current;
    }

    public boolean allowsNaturalSpawn(EntityType<?> type) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (type.getCategory() != MobCategory.MONSTER || id == null
                || !ResourceLocation.DEFAULT_NAMESPACE.equals(id.getNamespace())) {
            return true;
        }
        if (!allowVanillaHostileSpawns) {
            return false;
        }
        if (ZOMBIE_FAMILY.contains(type)) {
            return spawnVanillaZombies;
        }
        if (SKELETON_FAMILY.contains(type)) {
            return spawnSkeletons;
        }
        if (type == EntityType.CREEPER) {
            return spawnCreepers;
        }
        return true;
    }

    private static boolean read(Properties properties, String key, boolean fallback) {
        return Boolean.parseBoolean(properties.getProperty(key, Boolean.toString(fallback)).trim());
    }

    private static void writeDefaults() {
        String defaults = String.join(System.lineSeparator(),
                "allowVanillaHostileSpawns=false",
                "spawnVanillaZombies=false",
                "spawnSkeletons=false",
                "spawnCreepers=false",
                "");
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, defaults, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            ZombieInfection.LOGGER.error("Could not create {}", PATH, exception);
        }
    }

    private static VanillaHostileSpawnConfig defaults() {
        return new VanillaHostileSpawnConfig(false, false, false, false);
    }
}
