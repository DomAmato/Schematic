package com.dyn.schematics.gui;

import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import com.dyn.schematics.block.ClaimBlockTileEntity;
import com.dyn.schematics.utils.SimpleItemStack;
import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ContainerClaimBlock extends Container {

	private ClaimBlockTileEntity tile;

	public ContainerClaimBlock(EntityPlayer player, World world, BlockPos pos) {

		tile = (ClaimBlockTileEntity) world.getTileEntity(pos);
		tile.getInventory().openInventory(player);

		int slot = 0;

		int i = 0;
		int maxSlots = 28;

		for (Entry<SimpleItemStack, Integer> entry : tile.getInventory().getTotalMaterials().entrySet()) {
			if (maxSlots <= 0) {
				break;
			}
			addSlotToContainer(new Slot(tile.getInventory(), slot++, 48 + (54 * (i % 4)), 36 + ((i++ / 4) * 18)) {
				/**
				 * Return whether this slot's stack can be taken from this slot.
				 */
				@Override
				public boolean canTakeStack(EntityPlayer playerIn) {
					return false;
				}

				/**
				 * Actualy only call when we want to render the white square effect over the
				 * slots. Return always True, except for the armor slot of the Donkey/Mule (we
				 * can't interact with the Undead and Skeleton horses)
				 */
				@Override
				public boolean isEnabled() {
					return getStack() != ItemStack.EMPTY;
				}

				/**
				 * Check if the stack is a valid item for this slot. Always true beside for the
				 * armor slots.
				 */
				@Override
				public boolean isItemValid(ItemStack stack) {
					return super.isItemValid(stack) && (stack.getItem() == entry.getKey().getItem());
				}

			});
			maxSlots--;
		}

		for (int i1 = 0; i1 < 9; ++i1) {
			for (int k1 = 0; k1 < 3; ++k1) {
				addSlotToContainer(new Slot(player.inventory, k1 + (i1 * 3) + 9, -82 + (k1 * 18), (i1 * 18)));
			}
		}

		for (int j1 = 0; j1 < 9; ++j1) {
			addSlotToContainer(new Slot(player.inventory, j1, -104, (j1 * 18)));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return true;
	}

	public boolean canScroll() {
		return tile.getInventory().getTotalMaterials().size() > 28;
	}

	/**
	 * Called when the container is closed.
	 */
	@Override
	public void onContainerClosed(EntityPlayer playerIn) {
		super.onContainerClosed(playerIn);
		tile.getInventory().closeInventory(playerIn);
	}

	/**
	 * Updates the gui slots ItemStack's based on scroll position.
	 */
	public void scrollTo(float pos) {
		int i = ((tile.getSchematic().getRequiredMaterials().size() + 3) / 4) - 5;
		int j = (int) (pos * i + 0.5D);

		if (j < 0) {
			j = 0;
		}

		List<ItemStack> entries = Lists.newArrayList();

		for (Block entry : tile.getSchematic().getRequiredMaterials().keySet()) {
			ItemStack stack = new ItemStack(entry);
			Random rand = new Random();
			if (stack.isEmpty()) {
				stack = new ItemStack(entry.getItemDropped(entry.getDefaultState(), rand, 0));
			}
			entries.add(stack);
		}

		for (int row = 0; row < 7; ++row) {
			for (int column = 0; column < 4; ++column) {
				int inventoryIndex = column + ((row + j) * 4);

				if ((inventoryIndex >= 0) && (inventoryIndex < tile.getInventory().getTotalMaterials().size())) {
					tile.getInventory().setInventorySlotContents(column + (row * 4), entries.get(inventoryIndex));
				} else {
					tile.getInventory().setInventorySlotContents(column + (row * 4), ItemStack.EMPTY);
				}
			}
		}
	}

	@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
		if (clickTypeIn == ClickType.THROW) {
			return player.inventory.getItemStack();
		}
		if (clickTypeIn == ClickType.PICKUP) {
			ItemStack itemstack1 = player.inventory.getItemStack();

			if ((slotId < Math.min(tile.getInventory().getTotalMaterials().size(), 28)) && !itemstack1.isEmpty()) {
				ItemStack itemstack = tile.getInventory().addItem(itemstack1);
				tile.markForUpdate();
				return itemstack;
			}
		}
		return super.slotClick(slotId, dragType, clickTypeIn, player);
	}

	/**
	 * Handle when the stack in slot {@code index} is shift-clicked. Normally this
	 * moves the stack between the player inventory and the other inventory(s).
	 */
	@Override
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = inventorySlots.get(index);

		if ((slot != null) && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			itemstack1 = tile.getInventory().addItem(itemstack1);
			tile.markForUpdate();

			if (itemstack1.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}

			if (itemstack1.getCount() == itemstack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(playerIn, itemstack1);
		}

		return itemstack;
	}
}
