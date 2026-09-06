package com.carloshdzz22.zombieinfection.client.gui;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class ZombieTitleScreenTheme {
    private static final int PANEL_X = 18;
    private static final int PANEL_WIDTH = 214;
    private static final int BUTTON_X = PANEL_X + 13;
    private static final int BUTTON_WIDTH = PANEL_WIDTH - 26;

    private ZombieTitleScreenTheme() {
    }

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof TitleScreen)) {
                return;
            }

            List<AbstractWidget> widgets = Screens.getButtons(screen);
            widgets.removeIf(widget -> widget instanceof ThemeBackdropWidget
                    || widget instanceof ThemeForegroundWidget);

            List<AbstractWidget> mainButtons = new ArrayList<>();
            List<AbstractWidget> compactButtons = new ArrayList<>();
            for (AbstractWidget widget : List.copyOf(widgets)) {
                if (widget instanceof Button && widget.getWidth() >= 80 && !isCopyright(widget)) {
                    mainButtons.add(widget);
                } else if (widget.getWidth() <= 40) {
                    compactButtons.add(widget);
                }
            }

            int rowStep = scaledHeight < 300 ? 22 : 25;
            int panelHeight = 62 + mainButtons.size() * rowStep;
            int panelY = Math.max(54, Math.min(76, scaledHeight - panelHeight - 36));
            int buttonStartY = panelY + 48;

            for (int index = 0; index < mainButtons.size(); index++) {
                AbstractWidget button = mainButtons.get(index);
                button.setX(BUTTON_X);
                button.setY(buttonStartY + index * rowStep);
                button.setWidth(BUTTON_WIDTH);
                button.setAlpha(0.0F);
            }

            for (int index = 0; index < compactButtons.size(); index++) {
                AbstractWidget button = compactButtons.get(index);
                button.setX(PANEL_X + index * 24);
                button.setY(scaledHeight - 30);
            }

            widgets.addFirst(new ThemeBackdropWidget(
                    scaledWidth, scaledHeight, panelY, panelHeight, compactButtons));
            widgets.addLast(new ThemeForegroundWidget(
                    scaledWidth, scaledHeight, mainButtons, panelY, panelHeight));
        });
    }

    private static boolean isCopyright(AbstractWidget widget) {
        return widget.getMessage().getString().contains("Copyright Mojang");
    }

    private static final class ThemeBackdropWidget extends AbstractWidget {
        private final int panelY;
        private final int panelHeight;
        private final List<AbstractWidget> compactButtons;

        private ThemeBackdropWidget(int width, int height, int panelY, int panelHeight,
                List<AbstractWidget> compactButtons) {
            super(0, 0, width, height, Component.empty());
            this.panelY = panelY;
            this.panelHeight = panelHeight;
            this.compactButtons = List.copyOf(compactButtons);
            active = false;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY,
                float partialTick) {
            Font font = Minecraft.getInstance().font;
            graphics.fill(0, 0, Math.min(getWidth(), 270), getHeight(), 0x3102090D);

            graphics.fill(PANEL_X + 5, panelY + 6,
                    PANEL_X + PANEL_WIDTH + 6, panelY + panelHeight + 7, 0x79000000);
            graphics.fill(PANEL_X, panelY,
                    PANEL_X + PANEL_WIDTH, panelY + panelHeight, 0xE10A1217);
            graphics.renderOutline(PANEL_X, panelY, PANEL_WIDTH, panelHeight, 0xFF3A4C55);
            graphics.fill(PANEL_X, panelY,
                    PANEL_X + PANEL_WIDTH, panelY + 3, 0xFFC64247);
            graphics.fill(PANEL_X, panelY + 3,
                    PANEL_X + 3, panelY + panelHeight, 0xFF7C2D32);

            drawSignalMark(graphics, PANEL_X + 14, panelY + 14);
            graphics.drawString(font,
                    Component.translatable("menu.zombie-infection.title")
                            .withStyle(ChatFormatting.BOLD),
                    PANEL_X + 40, panelY + 12, 0xFFF1F5F6, false);
            graphics.drawString(font,
                    Component.translatable("menu.zombie-infection.subtitle"),
                    PANEL_X + 40, panelY + 25, 0xFF82959E, false);
            graphics.fill(PANEL_X + 12, panelY + 40,
                    PANEL_X + PANEL_WIDTH - 12, panelY + 41, 0xFF293940);
            graphics.fill(PANEL_X + 12, panelY + 40,
                    PANEL_X + 57, panelY + 41, 0xFFC64247);

            for (AbstractWidget button : compactButtons) {
                graphics.fill(button.getX() - 2, button.getY() - 2,
                        button.getX() + button.getWidth() + 2,
                        button.getY() + button.getHeight() + 2, 0x8D000000);
            }

            drawNetworkStatus(graphics, font);
            drawDirective(graphics, font);
        }

        private void drawNetworkStatus(GuiGraphics graphics, Font font) {
            if (getWidth() < 520) {
                return;
            }

            Component network = Component.translatable("menu.zombie-infection.network");
            int labelWidth = Math.max(font.width(network), font.width("v0.4.0  //  CC0-1.0"));
            int right = getWidth() - 16;
            int left = right - labelWidth - 27;
            graphics.fill(left, 12, right, 43, 0xA70A1217);
            graphics.renderOutline(left, 12, right - left, 31, 0xB23A4C55);
            graphics.fill(left + 9, 21, left + 14, 26, 0xFF58D991);
            graphics.drawString(font, network, left + 20, 17, 0xFFD0DADF, false);
            graphics.drawString(font, "v0.4.0  //  CC0-1.0",
                    left + 20, 29, 0xFF72858E, false);
        }

        private void drawDirective(GuiGraphics graphics, Font font) {
            int cardWidth = 210;
            int cardHeight = 63;
            int cardX = getWidth() - cardWidth - 18;
            int cardY = getHeight() - cardHeight - 39;
            if (cardX <= PANEL_X + PANEL_WIDTH + 24 || cardY < 54) {
                return;
            }

            graphics.fill(cardX + 4, cardY + 5,
                    cardX + cardWidth + 5, cardY + cardHeight + 6, 0x6C000000);
            graphics.fill(cardX, cardY,
                    cardX + cardWidth, cardY + cardHeight, 0xCD0A1217);
            graphics.renderOutline(cardX, cardY, cardWidth, cardHeight, 0xD13A4C55);
            graphics.fill(cardX, cardY, cardX + cardWidth, cardY + 3, 0xFFC64247);
            graphics.drawString(font,
                    Component.translatable("menu.zombie-infection.directive")
                            .withStyle(ChatFormatting.BOLD),
                    cardX + 10, cardY + 11, 0xFFE4ECEF, false);
            drawWrapped(graphics, font,
                    Component.translatable("menu.zombie-infection.directive.text"),
                    cardX + 10, cardY + 27, cardWidth - 20, 3, 0xFF8EA1AA);
        }

        private static void drawSignalMark(GuiGraphics graphics, int x, int y) {
            graphics.fill(x, y + 7, x + 4, y + 13, 0xFFC64247);
            graphics.fill(x + 6, y + 4, x + 10, y + 13, 0xFFD2DCE0);
            graphics.fill(x + 12, y, x + 16, y + 13, 0xFF72858E);
            graphics.fill(x - 1, y + 15, x + 17, y + 17, 0xFF35464E);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
        }
    }

    private static final class ThemeForegroundWidget extends AbstractWidget {
        private final List<AbstractWidget> mainButtons;
        private final int panelY;
        private final int panelHeight;

        private ThemeForegroundWidget(int width, int height, List<AbstractWidget> mainButtons,
                int panelY, int panelHeight) {
            super(0, 0, width, height, Component.empty());
            this.mainButtons = List.copyOf(mainButtons);
            this.panelY = panelY;
            this.panelHeight = panelHeight;
            active = false;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY,
                float partialTick) {
            Font font = Minecraft.getInstance().font;
            for (int index = 0; index < mainButtons.size(); index++) {
                AbstractWidget button = mainButtons.get(index);
                if (!button.visible) {
                    continue;
                }

                boolean selected = button.isMouseOver(mouseX, mouseY) || button.isFocused();
                int x = button.getX();
                int y = button.getY();
                int width = button.getWidth();
                int height = button.getHeight();
                int border = selected ? 0xFFD75459 : 0xFF34454D;
                int fill = selected ? 0xEE26343A : 0xE9111A1F;
                int label = button.active ? 0xFFF0F4F5 : 0xFF66757C;

                graphics.fill(x + 2, y + 3, x + width + 3, y + height + 3, 0x72000000);
                graphics.fill(x, y, x + width, y + height, border);
                graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, fill);
                graphics.fill(x + 1, y + 1, x + 4, y + height - 1,
                        selected ? 0xFFFF6C70 : 0xFFC64247);

                String sequence = String.format("%02d", index + 1);
                graphics.drawString(font, sequence, x + 9,
                        y + (height - 8) / 2, selected ? 0xFFFFA0A3 : 0xFF6F8189, false);
                graphics.drawCenteredString(font, button.getMessage(),
                        x + width / 2 + 7, y + (height - 8) / 2, label);

                if (selected && button.active) {
                    graphics.fill(x + width - 12, y + height / 2 - 1,
                            x + width - 7, y + height / 2 + 1, 0xFFFFD8D9);
                    graphics.fill(x + width - 8, y + height / 2 - 3,
                            x + width - 6, y + height / 2 + 3, 0xFFFFD8D9);
                }
            }

            int signalY = panelY + panelHeight - 13;
            graphics.drawString(font,
                    Component.translatable("menu.zombie-infection.signal"),
                    BUTTON_X, signalY, 0xFF657981, false);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
        }
    }

    private static void drawWrapped(GuiGraphics graphics, Font font, Component text,
            int x, int y, int width, int maxLines, int color) {
        List<net.minecraft.util.FormattedCharSequence> lines = font.split(text, width);
        for (int line = 0; line < Math.min(maxLines, lines.size()); line++) {
            graphics.drawString(font, lines.get(line), x, y + line * 10, color, false);
        }
    }
}
