package com.carloshdzz22.zombieinfection.screen;

import com.carloshdzz22.zombieinfection.block.entity.MedicalLabRecipes;
import com.carloshdzz22.zombieinfection.block.entity.MedicalLaboratoryBlockEntity;
import com.carloshdzz22.zombieinfection.registry.ModMenuTypes;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class MedicalLaboratoryMenu extends AbstractContainerMenu {
    private static final int LAB_SLOT_COUNT = MedicalLaboratoryBlockEntity.CONTAINER_SIZE;
    private static final int PLAYER_INVENTORY_START = LAB_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_START = PLAYER_INVENTORY_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final Container container;
    private final ContainerData data;

    public MedicalLaboratoryMenu(int id, Inventory playerInventory) {
        this(id, playerInventory, new SimpleContainer(LAB_SLOT_COUNT), new SimpleContainerData(2));
    }

    public MedicalLaboratoryMenu(int id, Inventory playerInventory, Container container, ContainerData data) {
        super(ModMenuTypes.MEDICAL_LABORATORY_MENU, id);
        checkContainerSize(container, LAB_SLOT_COUNT);
        checkContainerDataCount(data, 2);
        this.container = container;
        this.data = data;

        container.startOpen(playerInventory.player);

        addInputSlot(MedicalLaboratoryBlockEntity.SLOT_SAMPLE, 62, 17);
        addInputSlot(MedicalLaboratoryBlockEntity.SLOT_REAGENT, 80, 17);
        addInputSlot(MedicalLaboratoryBlockEntity.SLOT_CATALYST, 98, 17);
        this.addSlot(new Slot(container, MedicalLaboratoryBlockEntity.SLOT_RESULT, 80, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        this.addDataSlots(data);
    }

    private void addInputSlot(int slotIndex, int x, int y) {
        this.addSlot(new Slot(container, slotIndex, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return MedicalLabRecipes.isValidInput(slotIndex, stack);
            }
        });
    }

    public int getProgress() {
        return this.data.get(0);
    }

    public int getMaxProgress() {
        return this.data.get(1);
    }

    public boolean isProcessing() {
        return getProgress() > 0;
    }

    public int getScaledProgress() {
        int progress = getProgress();
        int maxProgress = getMaxProgress();
        return maxProgress > 0 && progress > 0 ? progress * 24 / maxProgress : 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack source = slot.getItem();
        ItemStack original = source.copy();

        if (index == MedicalLaboratoryBlockEntity.SLOT_RESULT) {
            if (!this.moveItemStackTo(source, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(source, original);
        } else if (index < LAB_SLOT_COUNT) {
            if (!this.moveItemStackTo(source, PLAYER_INVENTORY_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            boolean movedToLab = moveToMatchingLabSlot(source);
            if (!movedToLab && index < HOTBAR_START) {
                if (!this.moveItemStackTo(source, HOTBAR_START, HOTBAR_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!movedToLab && !this.moveItemStackTo(source, PLAYER_INVENTORY_START,
                    PLAYER_INVENTORY_END, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (source.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (source.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, source);
        return original;
    }

    private boolean moveToMatchingLabSlot(ItemStack stack) {
        if (MedicalLabRecipes.isSample(stack)) {
            return this.moveItemStackTo(stack, MedicalLaboratoryBlockEntity.SLOT_SAMPLE,
                    MedicalLaboratoryBlockEntity.SLOT_SAMPLE + 1, false);
        }
        if (MedicalLabRecipes.isReagent(stack)) {
            return this.moveItemStackTo(stack, MedicalLaboratoryBlockEntity.SLOT_REAGENT,
                    MedicalLaboratoryBlockEntity.SLOT_REAGENT + 1, false);
        }
        if (MedicalLabRecipes.isCatalyst(stack)) {
            return this.moveItemStackTo(stack, MedicalLaboratoryBlockEntity.SLOT_CATALYST,
                    MedicalLaboratoryBlockEntity.SLOT_CATALYST + 1, false);
        }
        return false;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                this.addSlot(new Slot(playerInventory, column + row * 9 + 9,
                        8 + column * 18, 84 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int column = 0; column < 9; ++column) {
            this.addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }
    }
}
