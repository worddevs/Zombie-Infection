package com.carloshdzz22.zombieinfection.infection;

import com.carloshdzz22.zombieinfection.ZombieInfection;
import com.carloshdzz22.zombieinfection.networking.InfectionAttachments;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;

public final class InfectionManager {
	public static final int MIN_INFECTION = 0;
	public static final int MAX_INFECTION = 100;
	public static final float ZOMBIE_INFECTION_CHANCE = 0.15F;
	public static final int ZOMBIE_INFECTION_MIN = 5;
	public static final int ZOMBIE_INFECTION_MAX = 12;
	public static final int EFFECT_CHECK_INTERVAL_TICKS = 20 * 10;

	private static final int SHORT_EFFECT_TICKS = 20 * 5;
	private static final int STANDARD_EFFECT_TICKS = 20 * 12;

	private InfectionManager() {
	}

	public static int getInfection(ServerPlayer player) {
		return clamp(player.getAttachedOrElse(InfectionAttachments.INFECTION, MIN_INFECTION));
	}

	public static int setInfection(ServerPlayer player, int value) {
		int previous = getInfection(player);
		int clamped = clamp(value);

		if (previous != clamped) {
			player.setAttached(InfectionAttachments.INFECTION, clamped);
		}

		if (clamped == MAX_INFECTION && previous < MAX_INFECTION && player.isAlive()) {
			succumb(player);
		}

		return clamped;
	}

	public static int addInfection(ServerPlayer player, int amount) {
		return setInfection(player, clamp((long) getInfection(player) + amount));
	}

	public static int reduceInfection(ServerPlayer player, int amount) {
		return setInfection(player, clamp((long) getInfection(player) - Math.max(0, amount)));
	}

	public static void applySuppression(ServerPlayer player, int durationTicks) {
		long now = serverTime(player);
		int duration = Math.max(0, durationTicks);
		long until = now > Long.MAX_VALUE - duration ? Long.MAX_VALUE : now + duration;
		player.setAttached(InfectionAttachments.SUPPRESSION_UNTIL, until);
	}

	public static long serverTime(ServerPlayer player) {
		return Math.max(0, player.level().getServer().overworld().getGameTime());
	}

	public static long suppressionRemainingTicks(ServerPlayer player) {
		long until = player.getAttachedOrElse(InfectionAttachments.SUPPRESSION_UNTIL, 0L);
		long now = serverTime(player);
		return until <= now ? 0 : until - now;
	}

	public static long suppressionRemainingSeconds(ServerPlayer player) {
		long ticks = suppressionRemainingTicks(player);
		return ticks / 20 + (ticks % 20 == 0 ? 0 : 1);
	}

	public static boolean isSuppressed(ServerPlayer player) {
		return suppressionRemainingTicks(player) > 0;
	}

	public static boolean tryInfect(ServerPlayer player, RandomSource random,
			com.carloshdzz22.zombieinfection.config.InfectionSource source) {
		var profile = com.carloshdzz22.zombieinfection.config.GameplayConfig.current().infection(source);
		return tryInfect(player, random, (float) profile.chance(), profile.minimum(), profile.maximum(), source.key());
	}

	public static void tryInfectFromZombie(ServerPlayer player, Zombie zombie) {
		if (zombie.getType() != EntityType.ZOMBIE || getInfection(player) >= MAX_INFECTION) {
			return;
		}

		tryInfect(player, zombie.getRandom(), ZOMBIE_INFECTION_CHANCE,
				ZOMBIE_INFECTION_MIN, ZOMBIE_INFECTION_MAX, "zombie");
	}

	public static boolean tryInfect(ServerPlayer player, RandomSource random, float chance,
			int minimumAmount, int maximumAmount, String sourceName) {
		if (!player.isAlive() || player.isSpectator() || getInfection(player) >= MAX_INFECTION
				|| !Float.isFinite(chance) || random.nextFloat() >= Math.clamp(chance, 0, 1)) {
			return false;
		}

		int minimum = Math.clamp(Math.min(minimumAmount, maximumAmount), 0, MAX_INFECTION);
		int maximum = Math.clamp(Math.max(minimumAmount, maximumAmount), 0, MAX_INFECTION);
		int amount = minimum + random.nextInt(maximum - minimum + 1);
		if (amount == 0) return false;
		if (isSuppressed(player)) {
			InfectionFeedback.notice(player, "message.zombie-infection.contagion_blocked");
			return false;
		}
		int previous = getInfection(player);
		int updated = addInfection(player, amount);
		int applied = updated - previous;
		InfectionFeedback.notice(player, "message.zombie-infection.infection_gain", applied);
		ZombieInfection.LOGGER.debug("{} was infected by {} (+{}; {}%)",
				player.getGameProfile().getName(), sourceName, applied, updated);
		return true;
	}

	public static void tick(MinecraftServer server) {
		if (server.getTickCount() % EFFECT_CHECK_INTERVAL_TICKS != 0) {
			return;
		}

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (player.isAlive()) {
				applyStageEffects(player, getInfection(player));
			}
		}
	}

	private static void applyStageEffects(ServerPlayer player, int infection) {
		if (infection < 25) {
			return;
		}

		if (infection < 50) {
			if (player.getRandom().nextFloat() < 0.35F) {
				player.addEffect(new MobEffectInstance(MobEffects.HUNGER, SHORT_EFFECT_TICKS, 0));
			}
			if (player.getRandom().nextFloat() < 0.20F) {
				player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, SHORT_EFFECT_TICKS, 0));
			}
			return;
		}

		if (infection < 75) {
			player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, STANDARD_EFFECT_TICKS, 0));
			player.addEffect(new MobEffectInstance(MobEffects.HUNGER, STANDARD_EFFECT_TICKS, 0));
			if (player.getRandom().nextFloat() < 0.35F) {
				player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, SHORT_EFFECT_TICKS, 0));
			}
			return;
		}

		player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, STANDARD_EFFECT_TICKS, 1));
		player.addEffect(new MobEffectInstance(MobEffects.HUNGER, STANDARD_EFFECT_TICKS, 0));
		if (player.getRandom().nextFloat() < 0.45F) {
			player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, SHORT_EFFECT_TICKS, 0));
		}
		if (player.getRandom().nextFloat() < 0.25F) {
			player.hurtServer(player.level(), player.damageSources().magic(), 1.0F);
		}
	}

	private static void succumb(ServerPlayer player) {
		player.sendSystemMessage(Component.translatable("message.zombie-infection.succumbed"));
		ZombieInfection.LOGGER.info("{} succumbed to infection", player.getGameProfile().getName());
		player.hurtServer(player.level(), player.damageSources().genericKill(), Float.MAX_VALUE);
	}

	private static int clamp(long value) {
		return (int) Math.max(MIN_INFECTION, Math.min(MAX_INFECTION, value));
	}
}
