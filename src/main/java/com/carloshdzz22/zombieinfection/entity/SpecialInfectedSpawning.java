package com.carloshdzz22.zombieinfection.entity;

import com.carloshdzz22.zombieinfection.ZombieInfection;
import com.carloshdzz22.zombieinfection.config.VanillaHostileSpawnConfig;
import com.carloshdzz22.zombieinfection.outbreak.OutbreakSpawnRules;
import com.carloshdzz22.zombieinfection.registry.ModEntities;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.Heightmap;

public final class SpecialInfectedSpawning {
    private SpecialInfectedSpawning() {
    }

    public static void initialize() {
        VanillaHostileSpawnConfig config = VanillaHostileSpawnConfig.load();
        SpawnPlacements.register(ModEntities.INFECTED, SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, OutbreakSpawnRules::checkInfectedSpawn);
        SpawnPlacements.register(ModEntities.RUNNER, SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, OutbreakSpawnRules::checkInfectedSpawn);
        SpawnPlacements.register(ModEntities.BLOATER, SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, OutbreakSpawnRules::checkInfectedSpawn);
        SpawnPlacements.register(ModEntities.SPITTER, SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, OutbreakSpawnRules::checkInfectedSpawn);

        var overworld = BiomeSelectors.foundInOverworld();
        BiomeModifications.addSpawn(overworld, MobCategory.MONSTER, ModEntities.INFECTED,
                OutbreakSpawnRules.MAX_COMMON_WEIGHT, 1, 4);
        BiomeModifications.addSpawn(overworld, MobCategory.MONSTER, ModEntities.RUNNER,
                OutbreakSpawnRules.MAX_RUNNER_WEIGHT, 1, 2);
        BiomeModifications.addSpawn(overworld, MobCategory.MONSTER, ModEntities.BLOATER,
                OutbreakSpawnRules.MAX_BLOATER_WEIGHT, 1, 1);
        BiomeModifications.addSpawn(overworld, MobCategory.MONSTER, ModEntities.SPITTER,
                OutbreakSpawnRules.MAX_SPITTER_WEIGHT, 1, 1);

        BiomeModifications.create(ZombieInfection.id("vanilla_hostile_spawn_filter"))
                .add(ModificationPhase.REMOVALS, overworld, context ->
                        context.getSpawnSettings().removeSpawns((category, entry) ->
                                shouldRemove(config, category, entry)));
    }

    private static boolean shouldRemove(VanillaHostileSpawnConfig config, MobCategory category,
            MobSpawnSettings.SpawnerData entry) {
        return category == MobCategory.MONSTER && !config.allowsNaturalSpawn(entry.type());
    }
}
