package com.carloshdzz22.zombieinfection.perception;

import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableMeleeAttack;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyPlayersSensor;

import java.util.List;

public final class InfectedBrainBehaviours {
    private InfectedBrainBehaviours() {
    }

    public static <E extends AbstractSpecialInfectedEntity<E>> List<? extends ExtendedSensor<? extends E>> sensors(E entity) {
        return List.of(new NearbyPlayersSensor<E>().setRadius(entity.brainVisionRange()));
    }

    public static <E extends AbstractSpecialInfectedEntity<E>> BrainActivityGroup<E> coreTasks() {
        return BrainActivityGroup.coreTasks(
                new LookAtTarget<E>().noTimeout(),
                new MoveToWalkTarget<E>());
    }

    public static <E extends AbstractSpecialInfectedEntity<E>> BrainActivityGroup<E> idleTasks() {
        return BrainActivityGroup.idleTasks(
                new AcquireVisiblePlayerBehaviour<E>(),
                new NoiseInvestigationBehaviour<E>(),
                new SetRandomWalkTarget<E>()
                        .speedModifier((entity, position) -> entity.brainPatrolSpeed())
                        .cooldownFor(entity -> entity.getRandom().nextIntBetweenInclusive(20, 50)));
    }

    public static <E extends AbstractSpecialInfectedEntity<E>> BrainActivityGroup<E> meleeFightTasks() {
        return BrainActivityGroup.fightTasks(
                new MaintainAttackTargetBehaviour<E>(),
                new PursueAttackTargetBehaviour<E>()
                        .cooldownFor(entity -> entity.brainPursuitUpdateInterval()),
                new AnimatableMeleeAttack<E>(0)
                        .attackInterval(entity -> entity.brainMeleeAttackInterval()));
    }
}
