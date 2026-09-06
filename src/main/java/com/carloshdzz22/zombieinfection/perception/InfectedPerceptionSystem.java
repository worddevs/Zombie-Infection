package com.carloshdzz22.zombieinfection.perception;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Common perception helpers shared by current and future infected. */
public final class InfectedPerceptionSystem {
    private InfectedPerceptionSystem() {
    }

    public static boolean canHear(AbstractSpecialInfectedEntity infected, NoiseEvent event) {
        double effectiveRadius = Math.min(infected.hearingRange(),
                event.radius() * infected.noiseSensitivity());
        return effectiveRadius > 0.0
                && infected.distanceToSqr(event.position()) <= effectiveRadius * effectiveRadius;
    }

    public static boolean canSee(AbstractSpecialInfectedEntity infected, LivingEntity target) {
        double range = infected.visionRange();
        return target.isAlive()
                && infected.distanceToSqr(target) <= range * range
                && infected.getSensing().hasLineOfSight(target);
    }

    public static boolean reached(AbstractSpecialInfectedEntity infected, Vec3 position) {
        return infected.distanceToSqr(position) <= 2.25;
    }
}
