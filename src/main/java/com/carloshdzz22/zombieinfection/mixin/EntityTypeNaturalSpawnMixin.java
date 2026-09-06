package com.carloshdzz22.zombieinfection.mixin;

import com.carloshdzz22.zombieinfection.config.VanillaHostileSpawnConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityType.class)
abstract class EntityTypeNaturalSpawnMixin<T extends Entity> {
    @Inject(
            method = "create(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/EntitySpawnReason;)Lnet/minecraft/world/entity/Entity;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void zombieInfection$filterNaturalHostiles(Level level, EntitySpawnReason reason,
            CallbackInfoReturnable<T> callbackInfo) {
        if ((reason == EntitySpawnReason.NATURAL || reason == EntitySpawnReason.PATROL)
                && !level.isClientSide() && level.dimension() == Level.OVERWORLD
                && !VanillaHostileSpawnConfig.current()
                        .allowsNaturalSpawn((EntityType<?>) (Object) this)) {
            callbackInfo.setReturnValue(null);
        }
    }
}
