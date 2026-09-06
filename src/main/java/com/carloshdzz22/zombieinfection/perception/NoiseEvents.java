package com.carloshdzz22.zombieinfection.perception;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/** Vanilla noise sources implemented for 0.4 without global infected scans. */
public final class NoiseEvents {
    private static final int SPRINT_INTERVAL_TICKS = 8;

    private NoiseEvents() {
    }

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(NoiseEvents::emitSprintNoise);
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            if (level instanceof ServerLevel serverLevel) {
                NoiseManager.emitBlockBreak(serverLevel, Vec3.atCenterOf(pos), player);
            }
        });
    }

    private static void emitSprintNoise(ServerLevel level) {
        if (level.getServer().getTickCount() % SPRINT_INTERVAL_TICKS != 0) {
            return;
        }
        for (ServerPlayer player : level.players()) {
            Vec3 movement = player.getDeltaMovement();
            if (player.isAlive() && !player.isSpectator() && player.isSprinting()
                    && player.onGround() && movement.horizontalDistanceSqr() > 0.0025) {
                NoiseManager.emitSprint(level, player);
            }
        }
    }
}
