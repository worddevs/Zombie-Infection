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

/** The ordinary population uses the same brain as the special infected. */
public final class CommonInfectedEntity extends AbstractSpecialInfectedEntity<CommonInfectedEntity> {
    public CommonInfectedEntity(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.24)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.ARMOR, 0.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0);
    }

    @Override
    protected CommonInfectedEntity self() {
        return this;
    }

    @Override
    protected double hearingRange() {
        return InfectedHearing.COMMON.range();
    }

    @Override
    protected double noiseSensitivity() {
        return InfectedHearing.COMMON.sensitivity();
    }

    @Override
    protected double visionRange() {
        return 32.0;
    }

    @Override
    protected int investigationTime() {
        return 20 * 8;
    }

    @Override
    protected int targetMemoryTime() {
        return 20 * 4;
    }

    @Override
    protected float patrolSpeed() {
        return 0.8F;
    }

    @Override
    protected float pursuitSpeed() {
        return 1.0F;
    }

    @Override
    protected int meleeAttackInterval() {
        return 25;
    }

    @Override
    protected int pursuitUpdateInterval() {
        return 10;
    }

    @Override
    public BrainActivityGroup<? extends CommonInfectedEntity> getFightTasks() {
        return meleeFightTasks();
    }

    @Override
    public void setBaby(boolean baby) {
        super.setBaby(false);
    }

    @Override
    public void setCanBreakDoors(boolean canBreakDoors) {
        super.setCanBreakDoors(false);
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    protected void randomizeReinforcementsChance() {
        getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).setBaseValue(0.0);
    }

    @Override
    protected void handleAttributes(float difficulty) {
        super.handleAttributes(difficulty);
        // Zombie leaders must not turn the common population into a source of reinforcements.
        getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).setBaseValue(0.0);
        getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).removeModifiers();
        getAttribute(Attributes.MAX_HEALTH).removeModifiers();
        setHealth(getMaxHealth());
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean damaged = super.doHurtTarget(level, target);
        if (damaged && target instanceof ServerPlayer player) {
            // The generic damage hook handles only EntityType.ZOMBIE, so this is the sole roll.
            InfectionManager.tryInfect(player, getRandom(), com.carloshdzz22.zombieinfection.config.InfectionSource.COMMON);
        }
        return damaged;
    }
}
