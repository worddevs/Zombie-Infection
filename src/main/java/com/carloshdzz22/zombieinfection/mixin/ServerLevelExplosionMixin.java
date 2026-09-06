package com.carloshdzz22.zombieinfection.mixin;

import com.carloshdzz22.zombieinfection.perception.NoiseManager;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Captures the single server explosion implementation after all public overloads converge. */
@Mixin(ServerLevel.class)
abstract class ServerLevelExplosionMixin {
    @Inject(
            method = "explode(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;Lnet/minecraft/core/particles/ParticleOptions;Lnet/minecraft/core/particles/ParticleOptions;Lnet/minecraft/core/Holder;)V",
            at = @At("HEAD")
    )
    private void zombieInfection$emitExplosionNoise(Entity source, DamageSource damageSource,
            ExplosionDamageCalculator calculator, double x, double y, double z, float power,
            boolean causesFire, Level.ExplosionInteraction interaction,
            ParticleOptions smallParticles, ParticleOptions largeParticles,
            Holder<SoundEvent> sound, CallbackInfo callbackInfo) {
        NoiseManager.emitExplosion((ServerLevel) (Object) this, new Vec3(x, y, z), power, source);
    }
}
