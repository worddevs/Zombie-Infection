package com.carloshdzz22.zombieinfection.entity;

import com.carloshdzz22.zombieinfection.infection.InfectionManager;
import com.carloshdzz22.zombieinfection.config.InfectionSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public final class InfectedCloudEntity extends AreaEffectCloud {
    private InfectionSource infectionSource = InfectionSource.BLOATER_CLOUD;

    public InfectedCloudEntity(EntityType<? extends AreaEffectCloud> type, Level level) {
        super(type, level);
        setCustomParticle(ParticleTypes.SPORE_BLOSSOM_AIR);
        setWaitTime(0);
        setRadiusOnUse(0.0F);
        setRadiusPerTick(0.0F);
    }

    public void configure(@org.jetbrains.annotations.Nullable LivingEntity owner, float radius, int duration,
                          InfectionSource source) {
        if (source != InfectionSource.BLOATER_CLOUD && source != InfectionSource.SPITTER_ZONE) {
            throw new IllegalArgumentException("Cloud requires a cloud infection source");
        }
        setOwner(owner);
        setRadius(radius);
        setDuration(duration);
        this.infectionSource = source;
    }

    public InfectionSource infectionSource() { return infectionSource; }

    @Override
    protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("infection_source", infectionSource.key());
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input) {
        super.readAdditionalSaveData(input);
        // Older clouds saved radius/duration but not infection values: recover their original category.
        String fallback = getRadius() <= 2.25F ? InfectionSource.SPITTER_ZONE.key() : InfectionSource.BLOATER_CLOUD.key();
        infectionSource = input.getStringOr("infection_source", fallback).equals(InfectionSource.SPITTER_ZONE.key())
                ? InfectionSource.SPITTER_ZONE : InfectionSource.BLOATER_CLOUD;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel level) || isRemoved() || tickCount % 20 != 0) {
            return;
        }

        double radiusSquared = getRadius() * getRadius();
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, getBoundingBox())) {
            if (player.isAlive() && !player.isSpectator() && distanceToSqr(player) <= radiusSquared) {
                InfectionManager.tryInfect(player, getRandom(), infectionSource);
            }
        }
    }
}
