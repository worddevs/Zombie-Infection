package com.carloshdzz22.zombieinfection.registry;

import com.carloshdzz22.zombieinfection.ZombieInfection;
import com.carloshdzz22.zombieinfection.entity.BloaterEntity;
import com.carloshdzz22.zombieinfection.entity.CommonInfectedEntity;
import com.carloshdzz22.zombieinfection.entity.InfectedCloudEntity;
import com.carloshdzz22.zombieinfection.entity.RunnerEntity;
import com.carloshdzz22.zombieinfection.entity.SpitterEntity;
import com.carloshdzz22.zombieinfection.entity.projectile.InfectedSpitEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {
    public static final EntityType<CommonInfectedEntity> INFECTED = register(
            "infected",
            EntityType.Builder.of(CommonInfectedEntity::new, MobCategory.MONSTER)
                    .sized(0.60F, 1.95F)
                    .eyeHeight(1.74F)
                    .clientTrackingRange(8)
    );
    public static final EntityType<RunnerEntity> RUNNER = register(
            "runner",
            EntityType.Builder.of(RunnerEntity::new, MobCategory.MONSTER)
                    .sized(0.56F, 1.80F)
                    .eyeHeight(1.62F)
                    .clientTrackingRange(8)
    );
    public static final EntityType<BloaterEntity> BLOATER = register(
            "bloater",
            EntityType.Builder.of(BloaterEntity::new, MobCategory.MONSTER)
                    .sized(0.72F, 2.20F)
                    .eyeHeight(1.90F)
                    .clientTrackingRange(8)
    );
    public static final EntityType<SpitterEntity> SPITTER = register(
            "spitter",
            EntityType.Builder.of(SpitterEntity::new, MobCategory.MONSTER)
                    .sized(0.60F, 1.95F)
                    .eyeHeight(1.74F)
                    .clientTrackingRange(10)
    );
    public static final EntityType<InfectedSpitEntity> INFECTED_SPIT = register(
            "infected_spit",
            EntityType.Builder.<InfectedSpitEntity>of(InfectedSpitEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(6)
                    .updateInterval(10)
                    .noLootTable()
    );
    public static final EntityType<InfectedCloudEntity> INFECTED_CLOUD = register(
            "infected_cloud",
            EntityType.Builder.<InfectedCloudEntity>of(InfectedCloudEntity::new, MobCategory.MISC)
                    .sized(6.0F, 0.5F)
                    .clientTrackingRange(8)
                    .updateInterval(10)
                    .noSummon()
                    .noSave()
                    .noLootTable()
    );

    private ModEntities() {
    }

    public static void initialize() {
        FabricDefaultAttributeRegistry.register(INFECTED, CommonInfectedEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(RUNNER, RunnerEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(BLOATER, BloaterEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(SPITTER, SpitterEntity.createAttributes());
        ZombieInfection.LOGGER.info("Registered Zombie Infection common and special infected entities");
    }

    private static <T extends Entity> EntityType<T> register(String path, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, ZombieInfection.id(path));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }
}
