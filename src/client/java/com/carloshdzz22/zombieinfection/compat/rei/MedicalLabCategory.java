package com.carloshdzz22.zombieinfection.compat.rei;

import com.carloshdzz22.zombieinfection.ZombieInfection;
import com.carloshdzz22.zombieinfection.registry.ModItems;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class MedicalLabCategory implements DisplayCategory<MedicalLabDisplay> {
    public static final CategoryIdentifier<MedicalLabDisplay> ID =
            CategoryIdentifier.of(ZombieInfection.MOD_ID, "medical_laboratory");

    @Override
    public CategoryIdentifier<? extends MedicalLabDisplay> getCategoryIdentifier() {
        return ID;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("category.zombie-infection.medical_laboratory");
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(ModItems.MEDICAL_LABORATORY);
    }

    @Override
    public List<Widget> setupDisplay(MedicalLabDisplay display, Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));

        int centerX = bounds.getCenterX();
        int y = bounds.getCenterY() - 8;
        for (int index = 0; index < display.getInputEntries().size(); index++) {
            widgets.add(Widgets.createSlot(new Point(centerX - 58 + index * 20, y))
                    .entries(display.getInputEntries().get(index))
                    .markInput());
        }

        widgets.add(Widgets.createArrow(new Point(centerX + 5, y)).animationDurationTicks(100));

        Point output = new Point(centerX + 43, y);
        widgets.add(Widgets.createResultSlotBackground(output));
        widgets.add(Widgets.createSlot(output)
                .entries(display.getOutputEntries().getFirst())
                .disableBackground()
                .markOutput());
        return widgets;
    }

    @Override
    public int getDisplayHeight() {
        return 40;
    }

    @Override
    public int getDisplayWidth(MedicalLabDisplay display) {
        return 132;
    }
}
