package com.carloshdzz22.zombieinfection;

import com.carloshdzz22.zombieinfection.command.InfectionCommands;
import com.carloshdzz22.zombieinfection.event.InfectionEvents;
import com.carloshdzz22.zombieinfection.entity.SpecialInfectedSpawning;
import com.carloshdzz22.zombieinfection.networking.InfectionAttachments;
import com.carloshdzz22.zombieinfection.perception.NoiseEvents;
import com.carloshdzz22.zombieinfection.registry.ModEntities;
import com.carloshdzz22.zombieinfection.registry.ModStructures;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZombieInfection implements ModInitializer {
	public static final String MOD_ID = "zombie-infection";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		com.carloshdzz22.zombieinfection.config.GameplayConfig.initialize();
		InfectionAttachments.initialize();
		com.carloshdzz22.zombieinfection.infection.InfectionFeedback.initialize();
		com.carloshdzz22.zombieinfection.outbreak.OutbreakNotifications.initialize();
		ModEntities.initialize();
		ModStructures.initialize();
		SpecialInfectedSpawning.initialize();
		com.carloshdzz22.zombieinfection.registry.ModBlocks.initialize();
		com.carloshdzz22.zombieinfection.registry.ModItems.initialize();
		com.carloshdzz22.zombieinfection.registry.ModBlockEntities.initialize();
		com.carloshdzz22.zombieinfection.registry.ModMenuTypes.initialize();
		InfectionEvents.register();
		NoiseEvents.register();
		InfectionCommands.register();

		LOGGER.info("Zombie Infection initialized");
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
