package com.carloshdzz22.zombieinfection.entity;

import com.carloshdzz22.zombieinfection.infection.InfectionManager;
import com.carloshdzz22.zombieinfection.registry.ModEntities;
import com.carloshdzz22.zombieinfection.perception.AbstractSpecialInfectedEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;

public final class BloaterEntity extends AbstractSpecialInfectedEntity<BloaterEntity> {
    private static final float INFECTION_CHANCE = 0.40F;
    private boolean releasedCloud;

    public BloaterEntity(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.18)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.65)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected BloaterEntity self() {
        return this;
    }

    @Override
    protected double hearingRange() {
        return 28.0;
    }

    @Override
    protected double visionRange() {
        return 30.0;
    }

    @Override
    protected double noiseSensitivity() {
        return 0.80;
    }

    @Override
    protected int investigationTime() {
        return 20 * 9;
    }

    @Override
    protected int targetMemoryTime() {
        return 20 * 8;
    }

    @Override
    protected float patrolSpeed() {
        return 0.65F;
    }

    @Override
    protected float pursuitSpeed() {
        return 0.82F;
    }

    @Override
    protected int meleeAttackInterval() {
        return 28;
    }

    @Override
    protected double investigationSpeed() {
        return 0.75;
    }

    @Override
    public BrainActivityGroup<? extends BloaterEntity> getFightTasks() {
        return meleeFightTasks();
    }

    @Override
    public void setBaby(boolean baby) {
        super.setBaby(false);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean damaged = super.doHurtTarget(level, target);
        if (damaged && target instanceof ServerPlayer player) {
            InfectionManager.tryInfect(player, getRandom(), INFECTION_CHANCE, 10, 18, "bloater");
        }
        return damaged;
    }

    @Override
    public void die(DamageSource source) {
        boolean shouldRelease = !releasedCloud && level() instanceof ServerLevel;
        if (shouldRelease) {
            releasedCloud = true;
        }
        super.die(source);
        if (shouldRelease && level() instanceof ServerLevel level) {
            InfectedCloudEntity cloud = new InfectedCloudEntity(ModEntities.INFECTED_CLOUD, level);
            cloud.setPos(getX(), getY(), getZ());
            cloud.configure(this, 3.5F, 100, 0.15F, 2, 5);
            level.addFreshEntity(cloud);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.HUSK_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.HUSK_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.HUSK_DEATH;
    }
}
