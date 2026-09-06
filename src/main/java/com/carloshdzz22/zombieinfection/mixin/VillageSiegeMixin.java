package com.carloshdzz22.zombieinfection.mixin;

import com.carloshdzz22.zombieinfection.config.VanillaHostileSpawnConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillageSiege.class)
abstract class VillageSiegeMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void zombieInfection$filterVillageSiege(ServerLevel level, boolean spawnEnemies,
            boolean spawnFriendlies, CallbackInfo callbackInfo) {
        if (level.dimension() == Level.OVERWORLD
                && !VanillaHostileSpawnConfig.current().allowsNaturalSpawn(EntityType.ZOMBIE)) {
            callbackInfo.cancel();
        }
    }
}
