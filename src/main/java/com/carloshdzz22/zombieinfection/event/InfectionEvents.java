package com.carloshdzz22.zombieinfection.event;

import com.carloshdzz22.zombieinfection.infection.InfectionManager;
import com.carloshdzz22.zombieinfection.registry.ModEntities;
import com.carloshdzz22.zombieinfection.registry.ModItems;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

public final class InfectionEvents {
	private InfectionEvents() {
	}

	public static void register() {
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) -> {
			if (!blocked && damageTaken > 0.0F && entity instanceof ServerPlayer player
					&& source.getEntity() instanceof Zombie zombie && zombie.getType() == EntityType.ZOMBIE) {
				InfectionManager.tryInfectFromZombie(player, zombie);
			}
		});

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
			if (entity.level() instanceof ServerLevel level && isExtractable(entity)
					&& damageSource.getEntity() instanceof ServerPlayer player) {
				tryExtraction(entity, level, player);
			}
		});

		ServerTickEvents.END_SERVER_TICK.register(InfectionManager::tick);
	}

	private static boolean isExtractable(LivingEntity entity) {
		return entity.getType() == EntityType.ZOMBIE
				|| entity.getType() == ModEntities.RUNNER
				|| entity.getType() == ModEntities.BLOATER
				|| entity.getType() == ModEntities.SPITTER;
	}

	private static void tryExtraction(LivingEntity entity, ServerLevel level, ServerPlayer player) {
		InteractionHand kitHand = findKitHand(player);
		if (kitHand == null || entity.getRandom().nextFloat() >= 0.60F) {
			return;
		}

		float tissueChance = tissueChance(entity);
		Item material = entity.getRandom().nextFloat() < tissueChance
				? ModItems.INFECTED_TISSUE
				: ModItems.INFECTED_BLOOD_SAMPLE;
		entity.spawnAtLocation(level, material);

		if (!player.isCreative()) {
			player.getItemInHand(kitHand).hurtAndBreak(1, level, player, ignored -> {
			});
		}
	}

	private static InteractionHand findKitHand(ServerPlayer player) {
		if (player.getOffhandItem().is(ModItems.EXTRACTION_KIT)) {
			return InteractionHand.OFF_HAND;
		}
		if (player.getMainHandItem().is(ModItems.EXTRACTION_KIT)) {
			return InteractionHand.MAIN_HAND;
		}
		return null;
	}

	private static float tissueChance(LivingEntity entity) {
		if (entity.getType() == ModEntities.RUNNER) {
			return 0.75F;
		}
		if (entity.getType() == ModEntities.BLOATER) {
			return 0.40F;
		}
		if (entity.getType() == ModEntities.SPITTER) {
			return 0.50F;
		}
		return 0.65F;
	}
}
