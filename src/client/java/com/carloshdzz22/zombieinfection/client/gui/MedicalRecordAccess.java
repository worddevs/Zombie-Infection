package com.carloshdzz22.zombieinfection.client.gui;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.Map;
import java.util.WeakHashMap;

/** Adds the primary Medical Record entry point to vanilla inventory screens. */
public final class MedicalRecordAccess {
    private static final Map<Screen, Button> CURRENT_BUTTONS = new WeakHashMap<>();

    private MedicalRecordAccess() {
    }

    public static void registerInventoryButton() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!isVanillaInventory(screen)) {
                return;
            }

            boolean creative = screen instanceof CreativeModeInventoryScreen;
            int halfWidth = creative ? 98 : 88;
            int x = scaledWidth / 2 - halfWidth - 23;
            int y = Math.max(4, scaledHeight / 2 - (creative ? 68 : 83));
            Button button = Button.builder(
                            Component.translatable("screen.zombie-infection.record.open.short"),
                            ignored -> open(client))
                    .bounds(x, y, 20, 20)
                    .build();
            button.setTooltip(Tooltip.create(
                    Component.translatable("screen.zombie-infection.record.open")));
            Screens.getButtons(screen).add(button);

            if (CURRENT_BUTTONS.put(screen, button) == null) {
                ScreenMouseEvents.afterMouseClick(screen).register(
                        (clickedScreen, mouseX, mouseY, mouseButton) -> {
                            Button current = CURRENT_BUTTONS.get(clickedScreen);
                            if (client.screen == clickedScreen
                                    && mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT
                                    && current != null && current.active && current.visible
                                    && current.isMouseOver(mouseX, mouseY)) {
                                open(client);
                            }
                        });
            }
        });
    }

    public static void open(Minecraft client) {
        if (client.player == null) {
            return;
        }
        Screen parent = client.screen instanceof InfectionStatusScreen
                ? null
                : client.screen;
        client.setScreen(new InfectionStatusScreen(parent));
    }

    private static boolean isVanillaInventory(Screen screen) {
        if (screen instanceof InfectionStatusScreen) {
            return false;
        }
        return screen.getClass() == InventoryScreen.class
                || screen.getClass() == CreativeModeInventoryScreen.class;
    }
}
