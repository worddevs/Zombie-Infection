package com.carloshdzz22.zombieinfection.block.entity;

import com.carloshdzz22.zombieinfection.ZombieInfection;
import com.carloshdzz22.zombieinfection.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

/**
 * Single source of truth for the laboratory recipes used by both the machine and REI.
 */
public final class MedicalLabRecipes {
    private static final List<Recipe> RECIPES = List.of(
            recipe("viral_extract", List.of(ModItems.INFECTED_TISSUE, ModItems.INFECTED_BLOOD_SAMPLE),
                    Items.REDSTONE, Items.GLASS_BOTTLE, ModItems.VIRAL_EXTRACT),
            recipe("antiviral_compound", List.of(ModItems.VIRAL_EXTRACT),
                    Items.SUGAR, Items.HONEY_BOTTLE, ModItems.ANTIVIRAL_COMPOUND),
            recipe("basic_antiviral", List.of(ModItems.ANTIVIRAL_COMPOUND),
                    Items.GOLDEN_CARROT, Items.GLASS_BOTTLE, ModItems.BASIC_ANTIVIRAL),
            recipe("antiviral_injection", List.of(ModItems.BASIC_ANTIVIRAL),
                    Items.IRON_INGOT, Items.GHAST_TEAR, ModItems.ANTIVIRAL_INJECTION),
            recipe("cure", List.of(ModItems.ANTIVIRAL_INJECTION),
                    Items.GOLDEN_APPLE, Items.GHAST_TEAR, ModItems.CURE),
            recipe("adrenaline_injector", List.of(ModItems.RUNNER_ADRENAL_GLAND),
                    Items.SUGAR, Items.GLASS_BOTTLE, ModItems.ADRENALINE_INJECTOR),
            recipe("infection_suppressant", List.of(ModItems.BLOATER_TOXIN_SAC),
                    ModItems.SPITTER_ENZYME, Items.HONEY_BOTTLE, ModItems.INFECTION_SUPPRESSANT)
    );

    private MedicalLabRecipes() {
    }

    public static List<Recipe> recipes() {
        return RECIPES;
    }

    public static Optional<Recipe> findCraftable(Container container) {
        ItemStack sample = container.getItem(MedicalLaboratoryBlockEntity.SLOT_SAMPLE);
        ItemStack reagent = container.getItem(MedicalLaboratoryBlockEntity.SLOT_REAGENT);
        ItemStack catalyst = container.getItem(MedicalLaboratoryBlockEntity.SLOT_CATALYST);
        ItemStack result = container.getItem(MedicalLaboratoryBlockEntity.SLOT_RESULT);

        return RECIPES.stream()
                .filter(recipe -> recipe.matches(sample, reagent, catalyst))
                .filter(recipe -> canAcceptResult(result, recipe.resultStack()))
                .findFirst();
    }

    public static boolean craft(Container container, Recipe recipe, Level level, BlockPos pos) {
        ItemStack sample = container.getItem(MedicalLaboratoryBlockEntity.SLOT_SAMPLE);
        ItemStack reagent = container.getItem(MedicalLaboratoryBlockEntity.SLOT_REAGENT);
        ItemStack catalyst = container.getItem(MedicalLaboratoryBlockEntity.SLOT_CATALYST);
        ItemStack result = container.getItem(MedicalLaboratoryBlockEntity.SLOT_RESULT);
        ItemStack crafted = recipe.resultStack();

        if (!recipe.matches(sample, reagent, catalyst) || !canAcceptResult(result, crafted)) {
            return false;
        }

        sample.shrink(1);
        reagent.shrink(1);

        ItemStack remainder = catalyst.getItem().getCraftingRemainder();
        catalyst.shrink(1);
        if (!remainder.isEmpty()) {
            if (catalyst.isEmpty()) {
                container.setItem(MedicalLaboratoryBlockEntity.SLOT_CATALYST, remainder.copy());
            } else {
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                        remainder.copy());
            }
        }

        if (result.isEmpty()) {
            container.setItem(MedicalLaboratoryBlockEntity.SLOT_RESULT, crafted);
        } else {
            result.grow(crafted.getCount());
            container.setChanged();
        }
        return true;
    }

    public static boolean isSample(ItemStack stack) {
        return !stack.isEmpty() && RECIPES.stream()
                .anyMatch(recipe -> recipe.sampleOptions().contains(stack.getItem()));
    }

    public static boolean isReagent(ItemStack stack) {
        return !stack.isEmpty() && RECIPES.stream()
                .anyMatch(recipe -> stack.is(recipe.reagent()));
    }

    public static boolean isCatalyst(ItemStack stack) {
        return !stack.isEmpty() && RECIPES.stream()
                .anyMatch(recipe -> stack.is(recipe.catalyst()));
    }

    public static boolean isValidInput(int slot, ItemStack stack) {
        return switch (slot) {
            case MedicalLaboratoryBlockEntity.SLOT_SAMPLE -> isSample(stack);
            case MedicalLaboratoryBlockEntity.SLOT_REAGENT -> isReagent(stack);
            case MedicalLaboratoryBlockEntity.SLOT_CATALYST -> isCatalyst(stack);
            default -> false;
        };
    }

    private static boolean canAcceptResult(ItemStack current, ItemStack crafted) {
        if (current.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameComponents(current, crafted)
                && current.getCount() + crafted.getCount() <= current.getMaxStackSize();
    }

    private static Recipe recipe(String path, List<Item> samples, Item reagent, Item catalyst, Item result) {
        return new Recipe(ZombieInfection.id(path), samples, reagent, catalyst, result);
    }

    public record Recipe(ResourceLocation id, List<Item> sampleOptions, Item reagent, Item catalyst, Item result) {
        public Recipe {
            sampleOptions = List.copyOf(sampleOptions);
        }

        public boolean matches(ItemStack sample, ItemStack reagentStack, ItemStack catalystStack) {
            return sampleOptions.contains(sample.getItem())
                    && reagentStack.is(reagent)
                    && catalystStack.is(catalyst);
        }

        public ItemStack resultStack() {
            return new ItemStack(result);
        }
    }
}
