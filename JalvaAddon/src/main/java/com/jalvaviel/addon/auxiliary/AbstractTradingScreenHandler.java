package com.jalvaviel.addon.auxiliary;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;

public abstract class AbstractTradingScreenHandler extends AbstractRecipeScreenHandler<Inventory> {
    protected final World world;
    protected final Inventory inventory;

    protected AbstractTradingScreenHandler(ScreenHandlerType<?> type, int syncId,
                                        Inventory inventory, PlayerInventory playerInventory) {
        super(type, syncId);
        this.world = playerInventory.player.world;
        this.inventory = inventory;
        checkSize(inventory, 3);
        this.addSlot(new Slot(inventory, 0, 136, 37));
        this.addSlot(new Slot(inventory, 1, 162, 37));
        this.addSlot(new Slot(inventory, 2, 216, 34));

        int i; // Fill the inventory slots for the TradingScreen
        for(i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 108 + j * 18, 84 + i * 18));
            }
        }

        for(i = 0; i < 9; ++i) { // Fill the hotbar slots for the TradingScreen
            this.addSlot(new Slot(playerInventory, i, 108 + i * 18, 142));
        }
    }

    protected boolean isTradeable(ItemStack itemStack) {
        return itemStack.getItem() == Items.EMERALD;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot2 = (Slot)this.slots.get(slot);
        if (slot2 != null && slot2.hasStack()) {
            ItemStack itemStack2 = slot2.getStack();
            itemStack = itemStack2.copy();
            if (slot == 2) {
                if (!this.insertItem(itemStack2, 3, 39, true)) { // Checks if the player's inventory is full and the result from the trade can't be stored.
                    return ItemStack.EMPTY;
                }
                slot2.onQuickTransfer(itemStack2, itemStack); // Move the result from the trade to the inventory
            } else if (slot != 1 && slot != 0) {
                if (this.isTradeable(itemStack2)) {
                    if (!this.insertItem(itemStack2, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slot >= 3 && slot < 30) {
                    if (!this.insertItem(itemStack2, 30, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slot >= 30 && slot < 39 && !this.insertItem(itemStack2, 3, 30, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(itemStack2, 3, 39, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack2.isEmpty()) {
                slot2.setStack(ItemStack.EMPTY);
            } else {
                slot2.markDirty();
            }

            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot2.onTakeItem(player, itemStack2);
        }

        return itemStack;
    }
}
