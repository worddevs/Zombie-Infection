package com.carloshdzz22.zombieinfection.client.renderer;

import com.carloshdzz22.zombieinfection.client.config.HealthBarConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.lang.ref.WeakReference;

/** One bounded ray pick per client tick, shared by all four renderers. */
public final class InfectedTargeting {
    private static WeakReference<Entity> target = new WeakReference<>(null);

    private InfectedTargeting() { }

    public static boolean isTarget(Entity entity) {
        return target.get() == entity;
    }

    public static void tick(Minecraft client) {
        Entity picked = null;
        var settings = HealthBarConfig.current();
        var camera = client.gameRenderer.getMainCamera();
        Entity cameraEntity = client.getCameraEntity();
        if (settings.enabled() && !client.options.hideGui && client.level != null
                && cameraEntity != null && cameraEntity.level() == client.level && camera.isInitialized()) {
            Vec3 origin = camera.getPosition();
            Vec3 end = origin.add(new Vec3(camera.getLookVector()).scale(settings.range()));
            var wall = client.level.clip(new ClipContext(origin, end, ClipContext.Block.VISUAL,
                    ClipContext.Fluid.NONE, cameraEntity));
            if (wall.getType() != HitResult.Type.MISS) {
                end = wall.getLocation();
            }
            var hit = ProjectileUtil.getEntityHitResult(cameraEntity, origin, end,
                    new AABB(origin, end).inflate(0.5),
                    entity -> entity.isPickable() && entity.isAlive() && !entity.isSpectator() && !entity.isInvisible(),
                    origin.distanceToSqr(end));
            if (hit != null) {
                picked = hit.getEntity();
            }
        }
        if (target.get() != picked) {
            target = new WeakReference<>(picked);
        }
    }
}
