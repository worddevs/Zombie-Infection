package com.carloshdzz22.zombieinfection.entity;

import com.carloshdzz22.zombieinfection.infection.InfectionManager;
import com.carloshdzz22.zombieinfection.perception.AbstractSpecialInfectedEntity;
import com.carloshdzz22.zombieinfection.perception.InfectedHearing;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;

public final class RunnerEntity extends AbstractSpecialInfectedEntity<RunnerEntity> {

    public RunnerEntity(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.ARMOR, 1.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.05)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected RunnerEntity self() {
        return this;
    }

    @Override
    protected double hearingRange() {
        return InfectedHearing.RUNNER.range();
    }

    @Override
    protected double visionRange() {
        return 40.0;
    }

    @Override
    protected double noiseSensitivity() {
        return InfectedHearing.RUNNER.sensitivity();
    }

    @Override
    protected int investigationTime() {
        return 20 * 12;
    }

    @Override
    protected int targetMemoryTime() {
        return 20 * 4;
    }

    @Override
    protected float patrolSpeed() {
        return 1.0F;
    }

    @Override
    protected float pursuitSpeed() {
        return 1.3F;
    }

    @Override
    protected int meleeAttackInterval() {
        return 15;
    }

    @Override
    protected double investigationSpeed() {
        return 1.25;
    }

    @Override
    public BrainActivityGroup<? extends RunnerEntity> getFightTasks() {
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
            InfectionManager.tryInfect(player, getRandom(), com.carloshdzz22.zombieinfection.config.InfectionSource.RUNNER);
        }
        return damaged;
    }
}
