package com.carloshdzz22.zombieinfection.entity.projectile;

import com.carloshdzz22.zombieinfection.entity.InfectedCloudEntity;
import com.carloshdzz22.zombieinfection.infection.InfectionManager;
import com.carloshdzz22.zombieinfection.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public final class InfectedSpitEntity extends ThrowableItemProjectile {
    private static final int MAX_LIFETIME_TICKS = 20 * 8;

    public InfectedSpitEntity(EntityType<? extends InfectedSpitEntity> type, Level level) {
        super(type, level);
    }

    public InfectedSpitEntity(Level level, LivingEntity owner) {
        super(ModEntities.INFECTED_SPIT, owner, level, Items.SLIME_BALL.getDefaultInstance());
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SLIME_BALL;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.035;
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount > MAX_LIFETIME_TICKS) {
            discard();
            return;
        }
        if (level().isClientSide) {
            level().addParticle(ParticleTypes.SPORE_BLOSSOM_AIR, getX(), getY(), getZ(), 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!(level() instanceof ServerLevel level) || !(result.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        Entity owner = getOwner();
        boolean damaged = owner instanceof LivingEntity livingOwner
                ? player.hurtServer(level, damageSources().mobProjectile(this, livingOwner), 3.0F)
                : player.hurtServer(level, damageSources().magic(), 3.0F);
        if (damaged) {
            InfectionManager.tryInfect(player, getRandom(), com.carloshdzz22.zombieinfection.config.InfectionSource.SPITTER_PROJECTILE);
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20 * 3, 0));
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level() instanceof ServerLevel level) {
            InfectedCloudEntity cloud = new InfectedCloudEntity(ModEntities.INFECTED_CLOUD, level);
            cloud.setPos(getX(), getY(), getZ());
            cloud.configure(getOwner() instanceof LivingEntity owner ? owner : null, 2.25F, 80,
                    com.carloshdzz22.zombieinfection.config.InfectionSource.SPITTER_ZONE);
            level.addFreshEntity(cloud);
            level.broadcastEntityEvent(this, (byte) 3);
            discard();
        }
    }
}
