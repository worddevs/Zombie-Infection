package com.carloshdzz22.zombieinfection.networking;

import com.carloshdzz22.zombieinfection.ZombieInfection;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.codec.ByteBufCodecs;

/**
 * Persistent player data and its server-to-owner synchronization channel.
 */
public final class InfectionAttachments {
	public static final AttachmentType<Integer> INFECTION = AttachmentRegistry.create(
			ZombieInfection.id("infection"),
			builder -> builder
					.initializer(() -> 0)
					.persistent(Codec.intRange(0, 100))
					.syncWith(ByteBufCodecs.VAR_INT, AttachmentSyncPredicate.targetOnly())
	);
	public static final AttachmentType<Long> SUPPRESSION_UNTIL = AttachmentRegistry.create(
			ZombieInfection.id("suppression_until"),
			builder -> builder
					.initializer(() -> 0L)
					.persistent(Codec.LONG)
	);

	private InfectionAttachments() {
	}

	public static void initialize() {
		ZombieInfection.LOGGER.debug("Registered infection attachment {}", INFECTION.identifier());
	}
}
