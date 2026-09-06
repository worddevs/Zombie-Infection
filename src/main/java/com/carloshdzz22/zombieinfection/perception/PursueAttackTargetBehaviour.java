package com.carloshdzz22.zombieinfection.perception;

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

public final class PursueAttackTargetBehaviour<E extends AbstractSpecialInfectedEntity<E>>
        extends ExtendedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORIES = List.of(
            Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT),
            Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED),
            Pair.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED));

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORIES;
    }

    @Override
    protected void start(E entity) {
        LivingEntity target = BrainUtil.getTargetOfEntity(entity);
        if (target == null) {
            return;
        }
        if (InfectedPerceptionSystem.canSee(entity, target)) {
            BrainUtil.setMemory(entity, MemoryModuleType.LOOK_TARGET, new EntityTracker(target, true));
            if (entity.isWithinMeleeAttackRange(target)) {
                BrainUtil.clearMemory(entity, MemoryModuleType.WALK_TARGET);
            } else {
                BrainUtil.setMemory(entity, MemoryModuleType.WALK_TARGET,
                        new WalkTarget(new EntityTracker(target, false), entity.brainPursuitSpeed(), 1));
            }
            return;
        }
        Vec3 lastSeen = entity.lastSeenTargetPosition();
        if (lastSeen != null) {
            BlockPosTracker tracker = new BlockPosTracker(BlockPos.containing(lastSeen));
            BrainUtil.setMemory(entity, MemoryModuleType.LOOK_TARGET, tracker);
            BrainUtil.setMemory(entity, MemoryModuleType.WALK_TARGET,
                    new WalkTarget(lastSeen, entity.brainPursuitSpeed(), 1));
        }
    }
}
