package com.carloshdzz22.zombieinfection.client;

import com.carloshdzz22.zombieinfection.ZombieInfection;
import com.carloshdzz22.zombieinfection.client.gui.MedicalRecordAccess;
import com.carloshdzz22.zombieinfection.client.gui.ZombieTitleScreenTheme;
import com.carloshdzz22.zombieinfection.client.hud.InfectionHud;
import com.carloshdzz22.zombieinfection.client.renderer.SpecialInfectedRenderer;
import com.carloshdzz22.zombieinfection.registry.ModEntities;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import org.lwjgl.glfw.GLFW;

public class ZombieInfectionClient implements ClientModInitializer {
	private static final KeyMapping OPEN_INFECTION_STATUS = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.zombie-infection.open_infection_status",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_I,
			"key.categories.zombie-infection"
	));

	public static boolean matchesMedicalRecordKey(int keyCode, int scanCode) {
		return OPEN_INFECTION_STATUS.matches(keyCode, scanCode);
	}

	public static boolean matchesMedicalRecordMouse(int button) {
		return OPEN_INFECTION_STATUS.matchesMouse(button);
	}

	@Override
	public void onInitializeClient() {
		com.carloshdzz22.zombieinfection.client.config.HealthBarConfig.initialize();
		ClientTickEvents.END_CLIENT_TICK.register(
				com.carloshdzz22.zombieinfection.client.renderer.InfectedTargeting::tick);
		EntityRendererRegistry.register(ModEntities.INFECTED, context -> new SpecialInfectedRenderer<>(
				context, ZombieInfection.id("textures/entity/infected.png"), 1.0F));
		EntityRendererRegistry.register(ModEntities.RUNNER, context -> new SpecialInfectedRenderer<>(
				context, ZombieInfection.id("textures/entity/runner.png"), 0.92F));
		EntityRendererRegistry.register(ModEntities.BLOATER, context -> new SpecialInfectedRenderer<>(
				context, ZombieInfection.id("textures/entity/bloater.png"), 1.18F));
		EntityRendererRegistry.register(ModEntities.SPITTER, context -> new SpecialInfectedRenderer<>(
				context, ZombieInfection.id("textures/entity/spitter.png"), 0.98F));
		EntityRendererRegistry.register(ModEntities.INFECTED_SPIT, ThrownItemRenderer::new);
		EntityRendererRegistry.register(ModEntities.INFECTED_CLOUD, NoopRenderer::new);

		HudElementRegistry.attachElementAfter(
				VanillaHudElements.STATUS_EFFECTS,
				ZombieInfection.id("infection_hud"),
				InfectionHud::render
		);

		MedicalRecordAccess.registerInventoryButton();
		ZombieTitleScreenTheme.register();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (OPEN_INFECTION_STATUS.consumeClick()) {
				if (client.player != null
						&& (client.screen == null
						|| client.screen.getClass() == net.minecraft.client.gui.screens.inventory.InventoryScreen.class
						|| client.screen.getClass() == net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.class)) {
					MedicalRecordAccess.open(client);
				}
			}
		});

		net.minecraft.client.gui.screens.MenuScreens.register(
				com.carloshdzz22.zombieinfection.registry.ModMenuTypes.MEDICAL_LABORATORY_MENU,
				com.carloshdzz22.zombieinfection.client.gui.MedicalLaboratoryScreen::new
		);
	}
}
