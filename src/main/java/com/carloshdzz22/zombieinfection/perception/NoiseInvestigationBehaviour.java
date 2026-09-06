package com.carloshdzz22.zombieinfection.perception;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;

public final class NoiseInvestigationBehaviour<E extends AbstractSpecialInfectedEntity<E>>
        extends ExtendedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORIES = List.of(
            Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT),
            Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
            Pair.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED));

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORIES;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        Vec3 position = entity.lastNoisePosition();
        return position != null && entity.investigationTicks() > 0
                && !InfectedPerceptionSystem.reached(entity, position);
    }

    @Override
    protected void start(E entity) {
        Vec3 position = entity.lastNoisePosition();
        if (position == null) {
            return;
        }
        BrainUtil.setMemory(entity, MemoryModuleType.LOOK_TARGET,
                new BlockPosTracker(BlockPos.containing(position)));
        BrainUtil.setMemory(entity, MemoryModuleType.WALK_TARGET,
                new WalkTarget(position, (float) entity.brainInvestigationSpeed(), 1));
    }
}
