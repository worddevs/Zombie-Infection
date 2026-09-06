package com.carloshdzz22.zombieinfection.entity;

import com.carloshdzz22.zombieinfection.infection.InfectionManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public final class InfectedCloudEntity extends AreaEffectCloud {
    private float infectionChance = 0.15F;
    private int minimumInfection = 2;
    private int maximumInfection = 5;

    public InfectedCloudEntity(EntityType<? extends AreaEffectCloud> type, Level level) {
        super(type, level);
        setCustomParticle(ParticleTypes.SPORE_BLOSSOM_AIR);
        setWaitTime(0);
        setRadiusOnUse(0.0F);
        setRadiusPerTick(0.0F);
    }

    public void configure(LivingEntity owner, float radius, int duration, float chance,
                          int minimumInfection, int maximumInfection) {
        setOwner(owner);
        setRadius(radius);
        setDuration(duration);
        this.infectionChance = chance;
        this.minimumInfection = minimumInfection;
        this.maximumInfection = maximumInfection;
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
                InfectionManager.tryInfect(player, getRandom(), infectionChance,
                        minimumInfection, maximumInfection, "infected_cloud");
            }
        }
    }
}
