package com.carloshdzz22.zombieinfection.perception;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;

public final class MaintainAttackTargetBehaviour<E extends AbstractSpecialInfectedEntity<E>>
        extends ExtendedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORIES = List.of(
            Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT));

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORIES;
    }

    @Override
    protected void start(E entity) {
        LivingEntity target = BrainUtil.getTargetOfEntity(entity);
        double maximumDistance = entity.brainVisionRange() + 8.0;
        if (!(target instanceof ServerPlayer player)
                || !entity.isValidPlayerTarget(player)
                || entity.distanceToSqr(player) > maximumDistance * maximumDistance) {
            entity.forgetTarget();
            return;
        }
        entity.setTarget(player);
        if (InfectedPerceptionSystem.canSee(entity, player)) {
            entity.rememberVisibleTarget(player);
            BrainUtil.setForgettableMemory(entity, MemoryModuleType.ATTACK_TARGET,
                    player, entity.brainTargetMemoryTime());
        }
    }
}
