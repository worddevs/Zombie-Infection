package com.carloshdzz22.zombieinfection.perception;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AbstractSpecialInfectedEntity<E extends AbstractSpecialInfectedEntity<E>>
        extends Zombie implements SmartBrainOwner<E> {
    @Nullable
    private Vec3 lastNoisePosition;
    @Nullable
    private Vec3 lastSeenTargetPosition;
    private int investigationTicks;

    protected AbstractSpecialInfectedEntity(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    protected abstract E self();

    protected abstract double hearingRange();

    protected abstract double visionRange();

    protected abstract double noiseSensitivity();

    protected abstract int investigationTime();

    protected abstract int targetMemoryTime();

    protected abstract float patrolSpeed();

    protected abstract float pursuitSpeed();

    protected abstract int meleeAttackInterval();

    protected double investigationSpeed() {
        return 1.0;
    }

    @Override
    protected final void registerGoals() {
    }

    @Override
    protected Brain.Provider<?> brainProvider() {
        return new SmartBrainProvider<>(self());
    }

    @Override
    public List<? extends ExtendedSensor<? extends E>> getSensors() {
        return InfectedBrainBehaviours.sensors(self());
    }

    @Override
    public BrainActivityGroup<? extends E> getCoreTasks() {
        return InfectedBrainBehaviours.coreTasks();
    }

    @Override
    public BrainActivityGroup<? extends E> getIdleTasks() {
        return InfectedBrainBehaviours.idleTasks();
    }

    protected final BrainActivityGroup<? extends E> meleeFightTasks() {
        return InfectedBrainBehaviours.meleeFightTasks();
    }

    public final boolean hear(NoiseEvent event) {
        if (!isAlive() || BrainUtil.hasMemory(this, MemoryModuleType.ATTACK_TARGET)
                || !InfectedPerceptionSystem.canHear(this, event)) {
            return false;
        }
        lastNoisePosition = event.position();
        investigationTicks = investigationTime();
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && investigationTicks > 0) {
            investigationTicks--;
            if (lastNoisePosition != null && InfectedPerceptionSystem.reached(this, lastNoisePosition)) {
                clearInvestigation();
            } else if (investigationTicks == 0) {
                clearInvestigation();
            }
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        tickBrain(self());
        setTarget(BrainUtil.getTargetOfEntity(this));
    }

    @Nullable
    public final Vec3 lastNoisePosition() {
        return lastNoisePosition;
    }

    public final int investigationTicks() {
        return investigationTicks;
    }

    public final void clearInvestigation() {
        lastNoisePosition = null;
        investigationTicks = 0;
    }

    public final boolean isValidPlayerTarget(@Nullable Object target) {
        return target instanceof ServerPlayer player
                && player.isAlive()
                && !player.isSpectator()
                && !player.getAbilities().invulnerable;
    }

    public final void rememberVisibleTarget(ServerPlayer player) {
        lastSeenTargetPosition = player.position();
        clearInvestigation();
    }

    @Nullable
    public final Vec3 lastSeenTargetPosition() {
        return lastSeenTargetPosition;
    }

    public final void forgetTarget() {
        lastSeenTargetPosition = null;
        BrainUtil.clearMemory(this, MemoryModuleType.ATTACK_TARGET);
        BrainUtil.clearMemory(this, MemoryModuleType.WALK_TARGET);
        BrainUtil.clearMemory(this, MemoryModuleType.LOOK_TARGET);
        setTarget(null);
        getNavigation().stop();
    }

    final int brainTargetMemoryTime() {
        return targetMemoryTime();
    }

    final float brainPatrolSpeed() {
        return patrolSpeed();
    }

    final float brainPursuitSpeed() {
        return pursuitSpeed();
    }

    final int brainMeleeAttackInterval() {
        return meleeAttackInterval();
    }

    final double brainVisionRange() {
        return visionRange();
    }

    final double brainInvestigationSpeed() {
        return investigationSpeed();
    }
}
