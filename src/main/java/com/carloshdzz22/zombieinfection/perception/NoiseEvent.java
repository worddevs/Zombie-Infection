package com.carloshdzz22.zombieinfection.perception;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** Immutable, short-lived description of a sound-producing event. */
public record NoiseEvent(Vec3 position, double radius, NoiseType type, @Nullable Entity source) {
    public NoiseEvent {
        radius = Math.max(0.0, radius);
    }
}
