package com.carloshdzz22.zombieinfection.block.entity;

import com.carloshdzz22.zombieinfection.registry.ModBlockEntities;
import com.carloshdzz22.zombieinfection.screen.MedicalLaboratoryMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class MedicalLaboratoryBlockEntity extends BaseContainerBlockEntity implements MenuProvider {
    public static final int SLOT_SAMPLE = 0;
    public static final int SLOT_REAGENT = 1;
    public static final int SLOT_CATALYST = 2;
    public static final int SLOT_RESULT = 3;
    public static final int CONTAINER_SIZE = 4;
    public static final int MAX_PROGRESS = 100;

    private static final String PROGRESS_KEY = "Progress";
    private static final String ACTIVE_RECIPE_KEY = "ActiveRecipe";

    private final NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
    private int progress;
    @Nullable
    private ResourceLocation activeRecipeId;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> MedicalLaboratoryBlockEntity.this.progress;
                case 1 -> MAX_PROGRESS;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                MedicalLaboratoryBlockEntity.this.progress = Mth.clamp(value, 0, MAX_PROGRESS);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public MedicalLaboratoryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MEDICAL_LABORATORY, pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.zombie-infection.medical_laboratory");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new MedicalLaboratoryMenu(id, inventory, this, this.dataAccess);
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize(stack)) {
            stack.setCount(getMaxStackSize(stack));
        }
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return MedicalLabRecipes.isValidInput(slot, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
        setChanged();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items.clear();
        ContainerHelper.loadAllItems(input, items);
        progress = Mth.clamp(input.getIntOr(PROGRESS_KEY, 0), 0, MAX_PROGRESS);
        activeRecipeId = input.read(ACTIVE_RECIPE_KEY, ResourceLocation.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        output.putInt(PROGRESS_KEY, progress);
        if (activeRecipeId != null) {
            output.store(ACTIVE_RECIPE_KEY, ResourceLocation.CODEC, activeRecipeId);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state,
                            MedicalLaboratoryBlockEntity blockEntity) {
        if (level.isClientSide) {
            return;
        }

        Optional<MedicalLabRecipes.Recipe> recipe = MedicalLabRecipes.findCraftable(blockEntity);
        if (recipe.isEmpty()) {
            if (blockEntity.progress != 0 || blockEntity.activeRecipeId != null) {
                blockEntity.progress = 0;
                blockEntity.activeRecipeId = null;
                setChanged(level, pos, state);
            }
            return;
        }

        MedicalLabRecipes.Recipe matchedRecipe = recipe.get();
        if (!matchedRecipe.id().equals(blockEntity.activeRecipeId)) {
            blockEntity.activeRecipeId = matchedRecipe.id();
            blockEntity.progress = 0;
        }

        blockEntity.progress++;
        if (blockEntity.progress >= MAX_PROGRESS) {
            MedicalLabRecipes.craft(blockEntity, matchedRecipe, level, pos);
            blockEntity.progress = 0;
            blockEntity.activeRecipeId = null;
        }
        setChanged(level, pos, state);
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> loadedItems) {
        for (int i = 0; i < Math.min(loadedItems.size(), items.size()); i++) {
            items.set(i, loadedItems.get(i));
        }
    }
}
