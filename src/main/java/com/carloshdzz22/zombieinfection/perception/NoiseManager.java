package com.carloshdzz22.zombieinfection.perception;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** Emits local, ephemeral noise events and queries only entities inside that event's radius. */
public final class NoiseManager {
    public static final double SPRINT_RADIUS = 6.0;
    public static final double BLOCK_BREAK_RADIUS = 10.0;
    public static final double EXPLOSION_MIN_RADIUS = 35.0;
    public static final double EXPLOSION_MAX_RADIUS = 60.0;
    public static final double GUNSHOT_RADIUS = 44.0;
    public static final double SUPPRESSED_GUNSHOT_RADIUS = 9.0;

    private NoiseManager() {
    }

    public static int emit(ServerLevel level, Vec3 position, double radius, NoiseType type,
            @Nullable Entity source) {
        NoiseEvent event = new NoiseEvent(position, radius, type, source);
        AABB searchBox = new AABB(position, position).inflate(radius);
        int reactions = 0;
        for (AbstractSpecialInfectedEntity infected : level.getEntitiesOfClass(
                AbstractSpecialInfectedEntity.class, searchBox, Entity::isAlive)) {
            if (infected.hear(event)) {
                reactions++;
            }
        }
        return reactions;
    }

    public static int emitSprint(ServerLevel level, Entity source) {
        return emit(level, source.position(), SPRINT_RADIUS, NoiseType.SPRINT, source);
    }

    public static int emitBlockBreak(ServerLevel level, Vec3 position, @Nullable Entity source) {
        return emit(level, position, BLOCK_BREAK_RADIUS, NoiseType.BLOCK_BREAK, source);
    }

    public static int emitExplosion(ServerLevel level, Vec3 position, float power,
            @Nullable Entity source) {
        double radius = Math.max(EXPLOSION_MIN_RADIUS,
                Math.min(EXPLOSION_MAX_RADIUS, 32.0 + power * 4.0));
        return emit(level, position, radius, NoiseType.EXPLOSION, source);
    }

    public static int emitGunshot(ServerLevel level, Vec3 position, @Nullable Entity source) {
        return emit(level, position, GUNSHOT_RADIUS, NoiseType.GUNSHOT, source);
    }

    public static int emitSuppressedGunshot(ServerLevel level, Vec3 position,
            @Nullable Entity source) {
        return emit(level, position, SUPPRESSED_GUNSHOT_RADIUS,
                NoiseType.SUPPRESSED_GUNSHOT, source);
    }
}
