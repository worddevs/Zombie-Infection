package com.carloshdzz22.zombieinfection.client.hud;

import com.carloshdzz22.zombieinfection.client.gui.InfectionStage;
import com.carloshdzz22.zombieinfection.networking.InfectionAttachments;
import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class InfectionHud {
    private static final int PANEL_WIDTH = 176;
    private static final int PANEL_HEIGHT = 51;
    private static final int BAR_WIDTH = 162;
    private static final int BAR_HEIGHT = 7;
    private static final long CHANGE_FEEDBACK_MILLIS = 1_600L;
    private static final long STAGE_FEEDBACK_MILLIS = 2_200L;

    private static int lastInfection = -1;
    private static int changeDirection;
    private static int changeAmount;
    private static long changeFeedbackEnds;
    private static long stageFeedbackEnds;
    private static String stageFeedbackKey;

    private InfectionHud() {
    }

    public static void render(GuiGraphics graphics, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            lastInfection = -1;
            return;
        }

        int infection = client.player.getAttachedOrElse(InfectionAttachments.INFECTION, 0);
        long now = Util.getMillis();
        if (client.options.hideGui || client.screen != null) {
            lastInfection = infection;
            changeFeedbackEnds = 0L;
            stageFeedbackEnds = 0L;
            return;
        }

        updateFeedback(infection, now);
        if (infection <= 0 && now >= changeFeedbackEnds && now >= stageFeedbackEnds) {
            return;
        }

        int x = 10;
        int y = 10;
        int stageColor = InfectionStage.fromValue(infection).color();
        int borderColor = feedbackColor(stageColor, now, infection);

        graphics.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, borderColor);
        graphics.fill(x + 1, y + 1, x + PANEL_WIDTH - 1, y + PANEL_HEIGHT - 1, 0xE0121719);
        graphics.fill(x + 3, y + 3, x + 5, y + PANEL_HEIGHT - 3, stageColor);

        Component label = Component.translatable("hud.zombie-infection.label");
        Component percentage = Component.translatable("hud.zombie-infection.percentage", infection);
        graphics.drawString(client.font, label, x + 9, y + 6, 0xFFE7ECEC, true);
        graphics.drawString(client.font, percentage,
                x + PANEL_WIDTH - 7 - client.font.width(percentage), y + 6, stageColor, true);

        int barX = x + 7;
        int barY = y + 23;
        graphics.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, 0xFF30383A);
        graphics.fill(barX + 1, barY + 1, barX + BAR_WIDTH - 1, barY + BAR_HEIGHT - 1, 0xFF171C1E);

        int filledWidth = Math.round((BAR_WIDTH - 2) * (infection / 100.0F));
        if (filledWidth > 0) {
            graphics.fill(barX + 1, barY + 1, barX + 1 + filledWidth, barY + BAR_HEIGHT - 1, stageColor);
        }

        if (now < changeFeedbackEnds) {
            int overlay = changeDirection > 0 ? 0x55FFFFFF : 0x5549E0B5;
            graphics.fill(barX + 1, barY + 1, barX + 1 + filledWidth, barY + BAR_HEIGHT - 1, overlay);
        }

        int feedbackY = y + 36;
        if (now < stageFeedbackEnds && stageFeedbackKey != null) {
            int noticeColor = changeDirection > 0 ? 0xFFFF8B83 : 0xFF63D9B5;
            graphics.drawString(client.font, Component.translatable(stageFeedbackKey),
                    x + 7, feedbackY, noticeColor, false);
        } else if (infection >= 75) {
            graphics.drawString(client.font, Component.translatable("hud.zombie-infection.critical"),
                    x + 7, feedbackY, pulseColor(now), true);
        }

        if (now < changeFeedbackEnds) {
            String changeKey = changeDirection > 0
                    ? "hud.zombie-infection.change.increase"
                    : "hud.zombie-infection.change.decrease";
            Component change = Component.translatable(changeKey, changeAmount);
            int changeColor = changeDirection > 0 ? 0xFFFF8B83 : 0xFF63D9B5;
            graphics.drawString(client.font, change,
                    x + PANEL_WIDTH - 7 - client.font.width(change), feedbackY, changeColor, true);
        }
    }

    private static void updateFeedback(int infection, long now) {
        if (lastInfection >= 0 && infection != lastInfection) {
            changeDirection = Integer.compare(infection, lastInfection);
            changeAmount = Math.abs(infection - lastInfection);
            changeFeedbackEnds = now + CHANGE_FEEDBACK_MILLIS;

            InfectionStage previousStage = InfectionStage.fromValue(lastInfection);
            InfectionStage currentStage = InfectionStage.fromValue(infection);
            if (previousStage != currentStage) {
                stageFeedbackKey = currentStage.rank() > previousStage.rank()
                        ? "hud.zombie-infection.stage.progressed"
                        : "hud.zombie-infection.stage.improved";
                stageFeedbackEnds = now + STAGE_FEEDBACK_MILLIS;
            }
        }
        lastInfection = infection;
    }

    private static int feedbackColor(int stageColor, long now, int infection) {
        if (now < changeFeedbackEnds) {
            return changeDirection > 0 ? 0xFFFF7777 : 0xFF5AD6B1;
        }
        if (infection >= 75) {
            double pulse = (Math.sin(now / 260.0) + 1.0) * 0.5;
            int red = 120 + (int) (pulse * 95);
            return 0xFF000000 | red << 16 | 0x2020;
        }
        return stageColor;
    }

    private static int pulseColor(long now) {
        double pulse = (Math.sin(now / 280.0) + 1.0) * 0.5;
        int greenBlue = 72 + (int) (pulse * 42);
        return 0xFFFF0000 | greenBlue << 8 | greenBlue;
    }
}
