package com.carloshdzz22.zombieinfection.perception;

import com.carloshdzz22.zombieinfection.entity.SpitterEntity;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;

public final class SpitterCombatBehaviour extends ExtendedBehaviour<SpitterEntity> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORIES = List.of(
            Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT),
            Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED),
            Pair.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED));
    private long nextAttackTime;

    public SpitterCombatBehaviour() {
        noTimeout();
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORIES;
    }

    @Override
    protected void start(SpitterEntity entity) {
        nextAttackTime = entity.level().getGameTime() + 20;
    }

    @Override
    protected boolean shouldKeepRunning(SpitterEntity entity) {
        LivingEntity target = BrainUtil.getTargetOfEntity(entity);
        return target != null && target.isAlive();
    }

    @Override
    protected void tick(SpitterEntity entity) {
        LivingEntity target = BrainUtil.getTargetOfEntity(entity);
        if (target == null || !target.isAlive()) {
            return;
        }

        boolean canSee = InfectedPerceptionSystem.canSee(entity, target);
        double distanceSquared = entity.distanceToSqr(target);
        entity.turnToward(target);
        if (canSee) {
            BrainUtil.setMemory(entity, MemoryModuleType.LOOK_TARGET, new EntityTracker(target, true));
            positionForAttack(entity, target, distanceSquared);
        } else {
            moveToLastSeenPosition(entity);
        }

        double minimum = entity.retreatRange();
        double maximum = entity.maximumAttackRange();
        if (entity.level().getGameTime() >= nextAttackTime
                && distanceSquared >= minimum * minimum
                && distanceSquared <= maximum * maximum
                && canSee
                && entity.isTargetInFront(target)) {
            entity.performRangedAttack(target,
                    (float) (Math.sqrt(distanceSquared) / maximum));
            nextAttackTime = entity.level().getGameTime()
                    + entity.getRandom().nextIntBetweenInclusive(60, 80);
        }
    }

    private void positionForAttack(SpitterEntity entity, LivingEntity target, double distanceSquared) {
        double retreat = entity.retreatRange();
        double idealMinimum = entity.idealMinimumRange();
        double idealMaximum = entity.idealMaximumRange();
        if (distanceSquared < retreat * retreat) {
            Vec3 away = entity.position().subtract(target.position());
            if (away.lengthSqr() < 1.0E-6) {
                away = new Vec3(1.0, 0.0, 0.0);
            }
            Vec3 destination = entity.position().add(away.normalize().scale(6.0));
            BrainUtil.setMemory(entity, MemoryModuleType.WALK_TARGET,
                    new WalkTarget(destination, 1.15F, 1));
        } else if (distanceSquared > idealMaximum * idealMaximum) {
            BrainUtil.setMemory(entity, MemoryModuleType.WALK_TARGET,
                    new WalkTarget(new EntityTracker(target, false), 1.0F, 8));
        } else if (distanceSquared < idealMinimum * idealMinimum) {
            Vec3 away = entity.position().subtract(target.position()).normalize();
            Vec3 destination = entity.position().add(away.scale(3.0));
            BrainUtil.setMemory(entity, MemoryModuleType.WALK_TARGET,
                    new WalkTarget(destination, 0.95F, 1));
        } else {
            BrainUtil.clearMemory(entity, MemoryModuleType.WALK_TARGET);
            entity.getNavigation().stop();
        }
    }

    private void moveToLastSeenPosition(SpitterEntity entity) {
        Vec3 lastSeen = entity.lastSeenTargetPosition();
        if (lastSeen == null) {
            return;
        }
        BrainUtil.setMemory(entity, MemoryModuleType.LOOK_TARGET,
                new BlockPosTracker(BlockPos.containing(lastSeen)));
        BrainUtil.setMemory(entity, MemoryModuleType.WALK_TARGET,
                new WalkTarget(lastSeen, 1.0F, 1));
    }

    @Override
    protected void stop(SpitterEntity entity) {
        entity.getNavigation().stop();
    }
}
