package com.carloshdzz22.zombieinfection.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class InfectionStatusScreen extends Screen {
    private final Screen parent;
    private MedicalRecordTab selectedTab = MedicalRecordTab.STATUS;

    public InfectionStatusScreen(Screen parent) {
        super(Component.translatable("screen.zombie-infection.status.title"));
        this.parent = parent;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        InfectionMonitorRenderer.render(graphics, font, minecraft,
                width, height, selectedTab, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (InfectionMonitorRenderer.closeAt(mouseX, mouseY, width, height)) {
                onClose();
                return true;
            }
            MedicalRecordTab clicked = InfectionMonitorRenderer.tabAt(
                    mouseX, mouseY, width, height);
            if (clicked != null) {
                selectedTab = clicked;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_I) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
