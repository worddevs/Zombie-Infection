package com.carloshdzz22.zombieinfection.client.gui;

import com.carloshdzz22.zombieinfection.networking.InfectionAttachments;
import com.carloshdzz22.zombieinfection.outbreak.OutbreakLevel;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

final class InfectionMonitorRenderer {
    private static final int MAX_WIDTH = 402;
    private static final int MAX_HEIGHT = 224;
    private static final int HEADER_HEIGHT = 31;
    private static final int TAB_HEIGHT = 24;

    private InfectionMonitorRenderer() {
    }

    static void render(GuiGraphics graphics, Font font, Minecraft minecraft,
            int screenWidth, int screenHeight, MedicalRecordTab selectedTab,
            int mouseX, int mouseY) {
        Layout layout = layout(screenWidth, screenHeight);
        int infection = minecraft != null && minecraft.player != null
                ? minecraft.player.getAttachedOrElse(InfectionAttachments.INFECTION, 0)
                : 0;
        InfectionStage stage = InfectionStage.fromValue(infection);

        graphics.fill(0, 0, screenWidth, screenHeight, 0xB0080C10);
        graphics.fill(layout.x() + 5, layout.y() + 6,
                layout.right() + 6, layout.bottom() + 7, 0x9A000000);
        graphics.fill(layout.x(), layout.y(), layout.right(), layout.bottom(), 0xF20B1116);
        graphics.renderOutline(layout.x(), layout.y(), layout.width(), layout.height(), 0xFF54636D);
        graphics.fill(layout.x() + 1, layout.y() + 1,
                layout.right() - 1, layout.y() + HEADER_HEIGHT, 0xFF151E25);
        graphics.fill(layout.x() + 1, layout.y() + HEADER_HEIGHT - 2,
                layout.right() - 1, layout.y() + HEADER_HEIGHT, stage.color());

        renderHeader(graphics, font, layout, stage, mouseX, mouseY);
        renderTabs(graphics, font, layout, selectedTab, mouseX, mouseY);

        int bodyY = layout.y() + HEADER_HEIGHT + TAB_HEIGHT + 7;
        int bodyBottom = layout.bottom() - 8;
        if (layout.wide()) {
            int summaryWidth = 126;
            renderSummary(graphics, font, minecraft, layout.x() + 8, bodyY,
                    summaryWidth, bodyBottom - bodyY, infection, stage);
            renderModule(graphics, font, minecraft, layout.x() + summaryWidth + 15,
                    bodyY, layout.width() - summaryWidth - 23, bodyBottom - bodyY,
                    infection, stage, selectedTab);
        } else {
            renderCompactSummary(graphics, font, minecraft, layout.x() + 8, bodyY,
                    layout.width() - 16, infection, stage);
            renderModule(graphics, font, minecraft, layout.x() + 8, bodyY + 53,
                    layout.width() - 16, bodyBottom - bodyY - 53,
                    infection, stage, selectedTab);
        }
    }

    static MedicalRecordTab tabAt(double mouseX, double mouseY,
            int screenWidth, int screenHeight) {
        Layout layout = layout(screenWidth, screenHeight);
        for (int index = 0; index < MedicalRecordTab.values().length; index++) {
            Rect rect = tabRect(layout, index);
            if (rect.contains(mouseX, mouseY)) {
                return MedicalRecordTab.values()[index];
            }
        }
        return null;
    }

    static boolean closeAt(double mouseX, double mouseY, int screenWidth, int screenHeight) {
        return closeRect(layout(screenWidth, screenHeight)).contains(mouseX, mouseY);
    }

    private static void renderHeader(GuiGraphics graphics, Font font, Layout layout,
            InfectionStage stage, int mouseX, int mouseY) {
        int iconX = layout.x() + 11;
        int iconY = layout.y() + 8;
        graphics.fill(iconX, iconY + 7, iconX + 3, iconY + 12, stage.color());
        graphics.fill(iconX + 5, iconY + 3, iconX + 8, iconY + 12, stage.color());
        graphics.fill(iconX + 10, iconY, iconX + 13, iconY + 12, stage.color());

        drawClipped(graphics, font,
                Component.translatable("screen.zombie-infection.monitor.title")
                        .withStyle(ChatFormatting.BOLD),
                iconX + 20, layout.y() + 7, layout.width() - 132, 0xFFE8F0F3);
        graphics.fill(iconX + 20, layout.y() + 20,
                iconX + 24, layout.y() + 24, 0xFF55D68A);
        drawClipped(graphics, font,
                Component.translatable("screen.zombie-infection.monitor.online"),
                iconX + 29, layout.y() + 18, layout.width() - 141, 0xFF8FA1AB);

        Rect close = closeRect(layout);
        boolean hovered = close.contains(mouseX, mouseY);
        graphics.fill(close.x(), close.y(), close.right(), close.bottom(),
                hovered ? 0xFF9C3E42 : 0xFF27333A);
        graphics.drawCenteredString(font, "X", close.x() + close.width() / 2,
                close.y() + 5, 0xFFF2F5F6);
    }

    private static void renderTabs(GuiGraphics graphics, Font font, Layout layout,
            MedicalRecordTab selectedTab, int mouseX, int mouseY) {
        for (int index = 0; index < MedicalRecordTab.values().length; index++) {
            MedicalRecordTab tab = MedicalRecordTab.values()[index];
            Rect rect = tabRect(layout, index);
            boolean active = tab == selectedTab;
            boolean hovered = rect.contains(mouseX, mouseY);
            graphics.fill(rect.x(), rect.y(), rect.right(), rect.bottom(),
                    active ? 0xFF202C33 : hovered ? 0xFF192329 : 0xFF10171C);
            if (active) {
                graphics.fill(rect.x() + 5, rect.bottom() - 2,
                        rect.right() - 5, rect.bottom(), 0xFF4DB8C7);
            }
            Component label = Component.translatable(tab.translationKey());
            if (font.width(label) > rect.width() - 10) {
                label = Component.translatable(tab.translationKey() + ".short");
            }
            drawCenteredClipped(graphics, font, label, rect.x() + 5, rect.y() + 7,
                    rect.width() - 10, active ? 0xFFEAF5F7 : 0xFF84949D);
        }
    }

    private static void renderSummary(GuiGraphics graphics, Font font, Minecraft minecraft,
            int x, int y, int width, int height, int infection, InfectionStage stage) {
        renderCard(graphics, x, y, width, height);
        drawClipped(graphics, font,
                Component.translatable("screen.zombie-infection.monitor.patient"),
                x + 9, y + 8, width - 18, 0xFF71838D);
        drawClipped(graphics, font,
                minecraft != null && minecraft.player != null
                        ? minecraft.player.getName() : Component.literal("—"),
                x + 9, y + 20, width - 18, 0xFFE7EEF1);
        graphics.fill(x + 9, y + 34, x + width - 9, y + 35, 0xFF263138);

        Component percentage = Component.translatable("hud.zombie-infection.percentage", infection)
                .withStyle(ChatFormatting.BOLD);
        graphics.drawCenteredString(font, percentage, x + width / 2, y + 47, stage.color());
        graphics.drawCenteredString(font, Component.translatable(stage.stageKey()),
                x + width / 2, y + 61, stage.color());
        renderMeter(graphics, x + 9, y + 78, width - 18, infection, stage.color());

        drawClipped(graphics, font,
                Component.translatable("screen.zombie-infection.monitor.risk"),
                x + 9, y + 96, width - 18, 0xFF71838D);
        drawWrapped(graphics, font, Component.translatable(stage.treatmentKey()),
                x + 9, y + 109, width - 18, 4, 0xFFCAD5D9);

        long day = minecraft != null && minecraft.level != null
                ? OutbreakLevel.day(minecraft.level.getDayTime()) : 0L;
        OutbreakLevel outbreak = OutbreakLevel.fromDay(day);
        int chipY = y + height - 30;
        graphics.fill(x + 8, chipY, x + width - 8, chipY + 21, 0xFF172126);
        graphics.fill(x + 8, chipY, x + 11, chipY + 21, outbreakColor(outbreak));
        drawClipped(graphics, font, Component.translatable(outbreak.nameKey()),
                x + 16, chipY + 3, width - 29, outbreakColor(outbreak));
        drawClipped(graphics, font,
                Component.translatable("screen.zombie-infection.record.outbreak.day", day),
                x + 16, chipY + 12, width - 29, 0xFF71838D);
    }

    private static void renderCompactSummary(GuiGraphics graphics, Font font, Minecraft minecraft,
            int x, int y, int width, int infection, InfectionStage stage) {
        renderCard(graphics, x, y, width, 45);
        Component player = minecraft != null && minecraft.player != null
                ? minecraft.player.getName() : Component.literal("—");
        drawClipped(graphics, font, player, x + 9, y + 7, width / 2 - 13, 0xFFE7EEF1);
        drawClipped(graphics, font, Component.translatable(stage.stageKey()),
                x + 9, y + 20, width / 2 - 13, stage.color());
        Component percentage = Component.translatable("hud.zombie-infection.percentage", infection);
        graphics.drawString(font, percentage, x + width - font.width(percentage) - 9,
                y + 7, stage.color(), false);
        renderMeter(graphics, x + width / 2, y + 23, width / 2 - 9,
                infection, stage.color());
    }

    private static void renderModule(GuiGraphics graphics, Font font, Minecraft minecraft,
            int x, int y, int width, int height, int infection, InfectionStage stage,
            MedicalRecordTab selectedTab) {
        renderCard(graphics, x, y, width, height);
        switch (selectedTab) {
            case STATUS -> renderStatus(graphics, font, x + 10, y + 9,
                    width - 20, height - 18, infection, stage);
            case TREATMENT -> renderTreatment(graphics, font, x + 10, y + 9,
                    width - 20, height - 18);
            case OUTBREAK -> renderOutbreak(graphics, font, minecraft, x + 10, y + 9,
                    width - 20, height - 18);
        }
    }

    private static void renderStatus(GuiGraphics graphics, Font font, int x, int y,
            int width, int height, int infection, InfectionStage stage) {
        renderSectionTitle(graphics, font, x, y, width,
                Component.translatable("screen.zombie-infection.monitor.reading"));
        Component load = Component.translatable("screen.zombie-infection.status.viral_load");
        Component percentage = Component.translatable("hud.zombie-infection.percentage", infection);
        drawClipped(graphics, font, load, x, y + 22,
                width - font.width(percentage) - 7, 0xFF95A6AE);
        graphics.drawString(font, percentage, x + width - font.width(percentage),
                y + 22, stage.color(), false);
        renderMeter(graphics, x, y + 36, width, infection, stage.color());

        drawClipped(graphics, font,
                Component.translatable("screen.zombie-infection.status.symptoms")
                        .withStyle(ChatFormatting.BOLD),
                x, y + 53, width, 0xFFDCE7EA);
        int lineY = y + 62;
        String[] symptoms = stage.symptomKeys();
        int visibleSymptoms = Math.min(symptoms.length, Math.max(1, (height - 58) / 18));
        for (int index = 0; index < visibleSymptoms; index++) {
            graphics.fill(x, lineY, x + width, lineY + 15, 0xFF162026);
            graphics.fill(x, lineY, x + 3, lineY + 15, stage.color());
            String symptomKey = symptoms[index];
            drawClipped(graphics, font, Component.translatable(symptomKey),
                    x + 9, lineY + 3, width - 14, 0xFFC7D2D6);
            lineY += 18;
        }
    }

    private static void renderTreatment(GuiGraphics graphics, Font font,
            int x, int y, int width, int height) {
        renderSectionTitle(graphics, font, x, y, width,
                Component.translatable("screen.zombie-infection.monitor.protocols"));
        int rowHeight = height < 125 ? 20 : 25;
        int rowGap = height < 125 ? 3 : 5;
        int firstY = y + 20;
        renderTreatmentRow(graphics, font, x, firstY, width, rowHeight,
                "item.zombie-infection.basic_antiviral",
                "screen.zombie-infection.record.treatment.basic", 0xFFE0BA4E);
        renderTreatmentRow(graphics, font, x, firstY + rowHeight + rowGap, width, rowHeight,
                "item.zombie-infection.antiviral_injection",
                "screen.zombie-infection.record.treatment.injection", 0xFF56C8BA);
        renderTreatmentRow(graphics, font, x, firstY + (rowHeight + rowGap) * 2, width, rowHeight,
                "item.zombie-infection.cure",
                "screen.zombie-infection.record.treatment.cure", 0xFFFF6568);
        if (height >= 145) {
            drawWrapped(graphics, font,
                    Component.translatable("screen.zombie-infection.record.treatment.produced"),
                    x, firstY + (rowHeight + rowGap) * 3 + 3, width, 3, 0xFF7F919A);
        }
    }

    private static void renderTreatmentRow(GuiGraphics graphics, Font font, int x, int y,
            int width, int height, String nameKey, String effectKey, int accent) {
        graphics.fill(x, y, x + width, y + height, 0xFF162026);
        graphics.fill(x, y, x + 4, y + height, accent);
        drawClipped(graphics, font, Component.translatable(nameKey).withStyle(ChatFormatting.BOLD),
                x + 10, y + 3, width - 15, 0xFFE2EAED);
        drawClipped(graphics, font, Component.translatable(effectKey),
                x + 10, y + height - 10, width - 15, accent);
    }

    private static void renderOutbreak(GuiGraphics graphics, Font font, Minecraft minecraft,
            int x, int y, int width, int height) {
        long day = minecraft != null && minecraft.level != null
                ? OutbreakLevel.day(minecraft.level.getDayTime()) : 0L;
        OutbreakLevel outbreak = OutbreakLevel.fromDay(day);
        int color = outbreakColor(outbreak);
        renderSectionTitle(graphics, font, x, y, width,
                Component.translatable("screen.zombie-infection.monitor.outbreak"));

        drawClipped(graphics, font,
                Component.translatable("screen.zombie-infection.record.outbreak.level", outbreak.index()),
                x, y + 23, width, 0xFF8799A2);
        drawClipped(graphics, font,
                Component.translatable(outbreak.nameKey()).withStyle(ChatFormatting.BOLD),
                x, y + 36, width, color);
        Component dayText = Component.translatable("screen.zombie-infection.record.outbreak.day", day);
        graphics.drawString(font, dayText, x + width - font.width(dayText),
                y + 36, 0xFFAAB8BE, false);

        int segmentGap = 3;
        int segmentWidth = Math.max(3, (width - segmentGap * 4) / 5);
        for (int index = 0; index < 5; index++) {
            int segmentX = x + index * (segmentWidth + segmentGap);
            graphics.fill(segmentX, y + 54, segmentX + segmentWidth, y + 63,
                    index <= outbreak.index()
                            ? outbreakColor(OutbreakLevel.values()[index]) : 0xFF263138);
        }
        if (height >= 105) {
            drawWrapped(graphics, font, Component.translatable(outbreak.descriptionKey()),
                    x, y + 72, width, height >= 145 ? 4 : 2, 0xFFCAD5D9);
        }
        if (height >= 145) {
            graphics.fill(x, y + 119, x + width, y + 120, 0xFF263138);
            drawWrapped(graphics, font,
                    Component.translatable("screen.zombie-infection.record.outbreak.population",
                            outbreak.localPopulationCap(), outbreak.localSpecialCap()),
                    x, y + 129, width, 3, 0xFF7F919A);
        }
    }

    private static void renderSectionTitle(GuiGraphics graphics, Font font,
            int x, int y, int width, Component title) {
        drawClipped(graphics, font, title.copy().withStyle(ChatFormatting.BOLD),
                x, y, width, 0xFFE4EDF0);
        graphics.fill(x, y + 14, x + width, y + 15, 0xFF2A373E);
        graphics.fill(x, y + 14, x + Math.min(42, width), y + 15, 0xFF4DB8C7);
    }

    private static void renderCard(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, 0xE8121A20);
        graphics.renderOutline(x, y, width, height, 0xFF2B3940);
    }

    private static void renderMeter(GuiGraphics graphics, int x, int y,
            int width, int infection, int color) {
        graphics.fill(x, y, x + width, y + 9, 0xFF273239);
        int fill = Math.round(width * infection / 100.0F);
        if (fill > 0) {
            graphics.fill(x, y, x + fill, y + 9, color);
        }
        for (int marker = 1; marker < 4; marker++) {
            int markerX = x + Math.round(width * marker / 4.0F);
            graphics.fill(markerX, y, markerX + 1, y + 9, 0xFF0B1116);
        }
    }

    private static int outbreakColor(OutbreakLevel level) {
        return switch (level) {
            case CONTAINED -> 0xFF55D68A;
            case EMERGING -> 0xFFE0C34E;
            case SPREADING -> 0xFFFF9A45;
            case SEVERE -> 0xFFFF6568;
            case CRITICAL -> 0xFFD94B8C;
        };
    }

    private static void drawCenteredClipped(GuiGraphics graphics, Font font, Component text,
            int x, int y, int maxWidth, int color) {
        String value = font.plainSubstrByWidth(text.getString(), Math.max(1, maxWidth));
        graphics.drawString(font, value, x + Math.max(0, (maxWidth - font.width(value)) / 2),
                y, color, false);
    }

    private static void drawClipped(GuiGraphics graphics, Font font, Component text,
            int x, int y, int maxWidth, int color) {
        List<FormattedCharSequence> lines = font.split(text, Math.max(1, maxWidth));
        if (!lines.isEmpty()) {
            graphics.drawString(font, lines.getFirst(), x, y, color, false);
        }
    }

    private static void drawWrapped(GuiGraphics graphics, Font font, Component text,
            int x, int y, int maxWidth, int maxLines, int color) {
        List<FormattedCharSequence> lines = font.split(text, Math.max(1, maxWidth));
        for (int line = 0; line < Math.min(maxLines, lines.size()); line++) {
            graphics.drawString(font, lines.get(line), x, y + line * 10, color, false);
        }
    }

    private static Layout layout(int screenWidth, int screenHeight) {
        int width = Math.min(MAX_WIDTH, Math.max(248, screenWidth - 24));
        int height = Math.min(MAX_HEIGHT, Math.max(196, screenHeight - 24));
        return new Layout((screenWidth - width) / 2, (screenHeight - height) / 2,
                width, height, width >= 350);
    }

    private static Rect tabRect(Layout layout, int index) {
        int width = (layout.width() - 16) / MedicalRecordTab.values().length;
        int x = layout.x() + 8 + index * width;
        if (index == MedicalRecordTab.values().length - 1) {
            width = layout.right() - 8 - x;
        }
        return new Rect(x, layout.y() + HEADER_HEIGHT, width, TAB_HEIGHT);
    }

    private static Rect closeRect(Layout layout) {
        return new Rect(layout.right() - 27, layout.y() + 6, 19, 19);
    }

    private record Layout(int x, int y, int width, int height, boolean wide) {
        int right() {
            return x + width;
        }

        int bottom() {
            return y + height;
        }
    }

    private record Rect(int x, int y, int width, int height) {
        int right() {
            return x + width;
        }

        int bottom() {
            return y + height;
        }

        boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < right() && mouseY >= y && mouseY < bottom();
        }
    }
}
