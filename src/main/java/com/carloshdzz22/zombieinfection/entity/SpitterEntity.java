package com.carloshdzz22.zombieinfection.entity;

import com.carloshdzz22.zombieinfection.entity.projectile.InfectedSpitEntity;
import com.carloshdzz22.zombieinfection.perception.AbstractSpecialInfectedEntity;
import com.carloshdzz22.zombieinfection.perception.InfectedPerceptionSystem;
import com.carloshdzz22.zombieinfection.perception.MaintainAttackTargetBehaviour;
import com.carloshdzz22.zombieinfection.perception.SpitterCombatBehaviour;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;

public final class SpitterEntity extends AbstractSpecialInfectedEntity<SpitterEntity>
        implements RangedAttackMob {
    private static final double MAX_ATTACK_RANGE = 14.0;
    private static final double IDEAL_MIN_RANGE = 7.0;
    private static final double IDEAL_MAX_RANGE = 11.0;
    private static final double RETREAT_RANGE = 4.5;
    private static final double FRONTAL_DOT_THRESHOLD = 0.6428;

    public SpitterEntity(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.ARMOR, 1.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.10)
                .add(Attributes.FOLLOW_RANGE, MAX_ATTACK_RANGE);
    }

    @Override
    protected SpitterEntity self() {
        return this;
    }

    @Override
    protected double hearingRange() {
        return 34.0;
    }

    @Override
    protected double visionRange() {
        return MAX_ATTACK_RANGE;
    }

    @Override
    protected double noiseSensitivity() {
        return 1.0;
    }

    @Override
    protected int investigationTime() {
        return 20 * 10;
    }

    @Override
    protected int targetMemoryTime() {
        return 20 * 5;
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
        return 24;
    }

    @Override
    public BrainActivityGroup<? extends SpitterEntity> getFightTasks() {
        return BrainActivityGroup.fightTasks(
                new MaintainAttackTargetBehaviour<SpitterEntity>(),
                new SpitterCombatBehaviour());
    }

    @Override
    public void setBaby(boolean baby) {
        super.setBaby(false);
    }

    public double maximumAttackRange() {
        return MAX_ATTACK_RANGE;
    }

    public double idealMinimumRange() {
        return IDEAL_MIN_RANGE;
    }

    public double idealMaximumRange() {
        return IDEAL_MAX_RANGE;
    }

    public double retreatRange() {
        return RETREAT_RANGE;
    }

    public void turnToward(LivingEntity target) {
        double x = target.getX() - getX();
        double z = target.getZ() - getZ();
        float desiredYaw = (float) (Mth.atan2(z, x) * 180.0 / Math.PI) - 90.0F;
        float turn = Mth.clamp(Mth.wrapDegrees(desiredYaw - getYRot()), -30.0F, 30.0F);
        float newYaw = getYRot() + turn;
        setYRot(newYaw);
        setYHeadRot(newYaw);
        getLookControl().setLookAt(target, 30.0F, 30.0F);
    }

    public boolean isTargetInFront(LivingEntity target) {
        Vec3 direction = target.position().subtract(position());
        Vec3 horizontal = new Vec3(direction.x, 0.0, direction.z);
        if (horizontal.lengthSqr() < 1.0E-6) {
            return true;
        }
        double yaw = getYRot() * Mth.DEG_TO_RAD;
        Vec3 forward = new Vec3(-Mth.sin((float) yaw), 0.0, Mth.cos((float) yaw));
        return forward.dot(horizontal.normalize()) >= FRONTAL_DOT_THRESHOLD;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (!(level() instanceof ServerLevel level)
                || !target.isAlive()
                || distanceToSqr(target) > MAX_ATTACK_RANGE * MAX_ATTACK_RANGE
                || !InfectedPerceptionSystem.canSee(this, target)
                || !isTargetInFront(target)) {
            return;
        }

        InfectedSpitEntity spit = new InfectedSpitEntity(level, this);
        double yaw = getYRot() * Mth.DEG_TO_RAD;
        spit.setPos(getX() - Mth.sin((float) yaw) * 0.65,
                getEyeY() - 0.15,
                getZ() + Mth.cos((float) yaw) * 0.65);
        double x = target.getX() - spit.getX();
        double z = target.getZ() - spit.getZ();
        double horizontalDistance = Math.sqrt(x * x + z * z);
        double y = target.getY(0.45) - spit.getY() + horizontalDistance * 0.12;
        spit.shoot(x, y, z, 1.05F, 3.0F);
        level.addFreshEntity(spit);
        playSound(SoundEvents.LLAMA_SPIT, 1.0F,
                0.85F + getRandom().nextFloat() * 0.2F);
    }
}
