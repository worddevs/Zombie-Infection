package com.carloshdzz22.zombieinfection.outbreak;

import com.carloshdzz22.zombieinfection.perception.AbstractSpecialInfectedEntity;
import com.carloshdzz22.zombieinfection.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;

/** Applies the day-based effective spawn weights without bypassing vanilla monster rules. */
public final class OutbreakSpawnRules {
    public static final int MAX_RUNNER_WEIGHT = 70;
    public static final int MAX_BLOATER_WEIGHT = 29;
    public static final int MAX_SPITTER_WEIGHT = 26;

    private OutbreakSpawnRules() {
    }

    public static boolean checkSpecialInfectedSpawn(EntityType<? extends Monster> type,
            ServerLevelAccessor levelAccessor, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        ServerLevel level = levelAccessor.getLevel();
        if (level.dimension() != Level.OVERWORLD
                || !Monster.checkMonsterSpawnRules(type, levelAccessor, reason, pos, random)) {
            return false;
        }

        OutbreakLevel outbreak = OutbreakLevel.from(level);
        if (reason == EntitySpawnReason.NATURAL
                && nearbySpecialInfected(level, pos) >= outbreak.localPopulationCap()) {
            return false;
        }
        int effectiveWeight;
        int registeredWeight;
        if (type == ModEntities.RUNNER) {
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

        double acceptance = outbreak.densityMultiplier() * effectiveWeight / registeredWeight;
        return random.nextDouble() < acceptance;
    }

    private static int nearbySpecialInfected(ServerLevel level, BlockPos pos) {
        AABB area = new AABB(pos).inflate(72.0, 32.0, 72.0);
        return level.getEntitiesOfClass(AbstractSpecialInfectedEntity.class, area,
                entity -> entity.isAlive()).size();
    }
}
