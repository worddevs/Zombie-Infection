package com.carloshdzz22.zombieinfection.compat.rei;

import com.carloshdzz22.zombieinfection.block.entity.MedicalLabRecipes;
import com.carloshdzz22.zombieinfection.registry.ModItems;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;

public final class ZombieInfectionReiClientPlugin implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new MedicalLabCategory());
        registry.addWorkstations(MedicalLabCategory.ID, EntryStacks.of(ModItems.MEDICAL_LABORATORY));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        MedicalLabRecipes.recipes().stream()
                .map(MedicalLabDisplay::new)
                .forEach(registry::add);
    }
}
