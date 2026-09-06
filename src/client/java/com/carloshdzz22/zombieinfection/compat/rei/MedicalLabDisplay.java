package com.carloshdzz22.zombieinfection.compat.rei;

import com.carloshdzz22.zombieinfection.block.entity.MedicalLabRecipes;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class MedicalLabDisplay extends BasicDisplay {
    public MedicalLabDisplay(MedicalLabRecipes.Recipe recipe) {
        super(List.of(
                        samples(recipe),
                        EntryIngredient.of(EntryStacks.of(recipe.reagent())),
                        EntryIngredient.of(EntryStacks.of(recipe.catalyst()))
                ),
                List.of(EntryIngredient.of(EntryStacks.of(recipe.result()))));
    }

    private static EntryIngredient samples(MedicalLabRecipes.Recipe recipe) {
        EntryIngredient.Builder builder = EntryIngredient.builder(recipe.sampleOptions().size());
        recipe.sampleOptions().forEach(item -> builder.add(EntryStacks.of(item)));
        return builder.build();
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return MedicalLabCategory.ID;
    }

    @Nullable
    @Override
    public DisplaySerializer<? extends BasicDisplay> getSerializer() {
        return null;
    }
}
