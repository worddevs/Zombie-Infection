package com.carloshdzz22.zombieinfection.outbreak;

import com.carloshdzz22.zombieinfection.perception.AbstractSpecialInfectedEntity;
import com.carloshdzz22.zombieinfection.registry.ModEntities;
import com.carloshdzz22.zombieinfection.config.GameplayConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/** Local infected-only spawning: sunlight is allowed; geometry and population limits remain. */
public final class OutbreakSpawnRules {
    public static final int MAX_COMMON_WEIGHT = 92;
    public static final int MAX_RUNNER_WEIGHT = 20;
    public static final int MAX_BLOATER_WEIGHT = 9;
    public static final int MAX_SPITTER_WEIGHT = 9;
    public static final double LOCAL_HORIZONTAL_RADIUS = 48.0;
    public static final double LOCAL_VERTICAL_RADIUS = 24.0;

    private OutbreakSpawnRules() {
    }

    public static boolean checkInfectedSpawn(EntityType<? extends Monster> type,
            ServerLevelAccessor levelAccessor, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        ServerLevel level = levelAccessor.getLevel();
        if ((reason == EntitySpawnReason.NATURAL && !GameplayConfig.current().naturalSpawning())
                || level.dimension() != Level.OVERWORLD
                || !Monster.checkAnyLightMonsterSpawnRules(type, levelAccessor, reason, pos, random)) {
            return false;
        }

        OutbreakLevel outbreak = OutbreakLevel.from(level);
        int effectiveWeight;
        int registeredWeight;
        if (type == ModEntities.INFECTED) {
            effectiveWeight = outbreak.commonWeight();
            registeredWeight = MAX_COMMON_WEIGHT;
        } else if (type == ModEntities.RUNNER) {
            effectiveWeight = outbreak.runnerWeight();
            registeredWeight = MAX_RUNNER_WEIGHT;
        } else if (type == ModEntities.BLOATER) {
            effectiveWeight = outbreak.bloaterWeight();
            registeredWeight = MAX_BLOATER_WEIGHT;
        } else if (type == ModEntities.SPITTER) {
            effectiveWeight = outbreak.spitterWeight();
            registeredWeight = MAX_SPITTER_WEIGHT;
        } else {
            return false;
        }

        double acceptance = Math.min(1, outbreak.densityMultiplier() * effectiveWeight / registeredWeight
                * timeOfDayMultiplier(type, level.getDayTime()) * GameplayConfig.current().densityMultiplier());
        // Reject cheap probability failures before querying the local population.
        if (random.nextDouble() >= acceptance) {
            return false;
        }
        return hasSuitableSite(type, level, reason, pos)
                && (reason != EntitySpawnReason.NATURAL || hasPopulationRoom(level, pos, type, outbreak));
    }

    static double timeOfDayMultiplier(EntityType<?> type, long dayTime) {
        if (Math.floorMod(dayTime, 24_000L) >= 12_000L) {
            return GameplayConfig.current().nightMultiplier();
        }
        if (type == ModEntities.INFECTED) {
            return GameplayConfig.current().dayCommonMultiplier();
        }
        return type == ModEntities.RUNNER ? GameplayConfig.current().dayRunnerMultiplier()
                : GameplayConfig.current().daySpecialMultiplier();
    }

    private static boolean hasSuitableSite(EntityType<?> type, ServerLevel level,
            EntitySpawnReason reason, BlockPos pos) {
        // Skylight is deliberately unrestricted; lamps/torches still protect well-lit interiors.
        if (level.getBrightness(LightLayer.BLOCK, pos) > GameplayConfig.current().maximumBlockLight()
                || level.getBlockState(pos.below()).is(BlockTags.LEAVES)) {
            return false;
        }
        double x = pos.getX() + 0.5;
        double z = pos.getZ() + 0.5;
        if (reason == EntitySpawnReason.NATURAL
                && level.hasNearbyAlivePlayer(x, pos.getY(), z, GameplayConfig.current().minimumPlayerDistance())) {
            return false;
        }
        AABB body = type.getDimensions().makeBoundingBox(x, pos.getY(), z);
        return level.getWorldBorder().isWithinBounds(body)
                && !level.containsAnyLiquid(body) && level.noCollision(body);
    }

    static boolean hasPopulationRoom(ServerLevel level, BlockPos pos,
            EntityType<?> type, OutbreakLevel outbreak) {
        int totalCap = effectiveTotalCap(outbreak);
        int specialCap = effectiveSpecialCap(outbreak);
        if (type != ModEntities.INFECTED && specialCap == 0) {
            return false;
        }
        AABB area = new AABB(pos).inflate(LOCAL_HORIZONTAL_RADIUS, LOCAL_VERTICAL_RADIUS,
                LOCAL_HORIZONTAL_RADIUS);
        List<AbstractSpecialInfectedEntity> nearby = new ArrayList<>();
        // Stop collecting at the total cap, even if commands created a very large crowd.
        level.getEntities(EntityTypeTest.forClass(AbstractSpecialInfectedEntity.class), area,
                Entity::isAlive, nearby, totalCap);
        if (nearby.size() >= totalCap) {
            return false;
        }
        if (type == ModEntities.INFECTED) {
            return true;
        }
        int specials = 0;
        for (AbstractSpecialInfectedEntity infected : nearby) {
            if (infected.getType() != ModEntities.INFECTED && ++specials >= specialCap) {
                return false;
            }
        }
        return true;
    }

    public static int effectiveTotalCap(OutbreakLevel outbreak) {
        return GameplayConfig.current().totalCap(outbreak.localPopulationCap());
    }

    public static int effectiveSpecialCap(OutbreakLevel outbreak) {
        return GameplayConfig.current().specialCap(outbreak.localSpecialCap(), effectiveTotalCap(outbreak));
    }
}
