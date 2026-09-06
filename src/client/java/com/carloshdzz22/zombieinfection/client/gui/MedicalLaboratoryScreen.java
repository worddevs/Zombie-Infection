package com.carloshdzz22.zombieinfection.client.gui;

import com.carloshdzz22.zombieinfection.screen.MedicalLaboratoryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MedicalLaboratoryScreen extends AbstractContainerScreen<MedicalLaboratoryMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/dispenser.png");
    private static final int GUI_BACKGROUND_COLOR = 0xFFC6C6C6;

    public MedicalLaboratoryScreen(MedicalLaboratoryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0f, 0f, this.imageWidth, this.imageHeight, 256, 256);

        guiGraphics.fill(x + 61, y + 34, x + 115, y + 52, GUI_BACKGROUND_COLOR);
        guiGraphics.fill(x + 61, y + 52, x + 79, y + 70, GUI_BACKGROUND_COLOR);
        guiGraphics.fill(x + 97, y + 52, x + 115, y + 70, GUI_BACKGROUND_COLOR);

        if (this.menu.isProcessing()) {
            int progress = this.menu.getScaledProgress();
            guiGraphics.fill(x + 75, y + 38, x + 101, y + 44, 0xFF555555);
            guiGraphics.fill(x + 76, y + 39, x + 76 + progress, y + 43, 0xFF27A844);
        }
    }
}
