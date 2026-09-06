package com.carloshdzz22.zombieinfection.perception;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;

public final class AcquireVisiblePlayerBehaviour<E extends AbstractSpecialInfectedEntity<E>>
        extends ExtendedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORIES = List.of(
            Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT),
            Pair.of(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, MemoryStatus.VALUE_PRESENT));
    private ServerPlayer target;

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORIES;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        Object candidate = BrainUtil.getMemory(entity, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER);
        target = candidate instanceof ServerPlayer player ? player : null;
        return entity.isValidPlayerTarget(target) && InfectedPerceptionSystem.canSee(entity, target);
    }

    @Override
    protected void start(E entity) {
        if (target == null) {
            return;
        }
        entity.rememberVisibleTarget(target);
        BrainUtil.setForgettableMemory(entity, MemoryModuleType.ATTACK_TARGET,
                target, entity.brainTargetMemoryTime());
        BrainUtil.setMemory(entity, MemoryModuleType.LOOK_TARGET, new EntityTracker(target, true));
        entity.setTarget(target);
        target = null;
    }
}
