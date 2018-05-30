package com.dyn.schematics.gui;

import com.dyn.schematics.Schematic;
import com.dyn.schematics.SchematicMod;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerArchitect extends Container {
	/** Here comes out item you merged and/or renamed. */
	private final IInventory outputSlot;
	/**
	 * The 2slots where you put your items in that you want to merge and/or rename.
	 */
	private final IInventory inputSlots;
	private final World world;
	private final BlockPos selfPosition;
	/** The maximum cost of repairing/renaming in the anvil. */
	public int cost;
	private String schematicName;
	/** The player that has this container open. */
	private NBTTagCompound nbtTag;

	@SideOnly(Side.CLIENT)
	public ContainerArchitect(EntityPlayer player, World worldIn) {
		this(player, worldIn, BlockPos.ORIGIN);
	}

	public ContainerArchitect(EntityPlayer player, final World worldIn, final BlockPos blockPosIn) {
		outputSlot = new InventoryCraftResult();
		player.inventory.openInventory(player);
		inputSlots = new InventoryBasic("Achitect", true, 2) {
			/**
			 * For tile entities, ensures the chunk containing the tile entity is saved to
			 * disk later - the game won't think it hasn't changed and skip it.
			 */
			@Override
			public void markDirty() {
				super.markDirty();
				ContainerArchitect.this.onCraftMatrixChanged(this);
			}
		};
		selfPosition = blockPosIn;
		world = worldIn;

		addSlotToContainer(new Slot(inputSlots, 0, -30, 120) {
			/**
			 * Check if the stack is a valid item for this slot. Always true beside for the
			 * armor slots.
			 */
			@Override
			public boolean isItemValid(ItemStack stack) {
				return super.isItemValid(stack) && (stack.getItem() == SchematicMod.schematic)
						&& (stack.getItem().getMetadata(0) == 0) && !getHasStack();
			}

		});
		addSlotToContainer(new Slot(inputSlots, 1, -30, 10) {
			/**
			 * Check if the stack is a valid item for this slot. Always true beside for the
			 * armor slots.
			 */
			@Override
			public boolean isItemValid(ItemStack stack) {
				return super.isItemValid(stack) && (stack.getItem() == Items.GOLD_INGOT) && !getHasStack();
			}

		});
		addSlotToContainer(new Slot(outputSlot, 2, 190, 120) {
			/**
			 * Return whether this slot's stack can be taken from this slot.
			 */
			@Override
			public boolean canTakeStack(EntityPlayer playerIn) {
				return (inputSlots.getStackInSlot(1).getCount() >= Schematic.getCost(outputSlot.getStackInSlot(0)))
						&& (Schematic.getCost(outputSlot.getStackInSlot(0)) > 0) && getHasStack();
			}

			/**
			 * Check if the stack is allowed to be placed in this slot, used for armor slots
			 * as well as furnace fuel.
			 */
			@Override
			public boolean isItemValid(ItemStack stack) {
				return false;
			}

			@Override
			public ItemStack onTake(EntityPlayer thePlayer, ItemStack stack) {

				inputSlots.setInventorySlotContents(0, ItemStack.EMPTY);

				ItemStack itemstack = inputSlots.getStackInSlot(1);

				if (!itemstack.isEmpty() && (itemstack.getCount() > Schematic.getCost(stack))) {
					itemstack.shrink(Schematic.getCost(stack));
					inputSlots.setInventorySlotContents(1, itemstack);
				} else {
					inputSlots.setInventorySlotContents(1, ItemStack.EMPTY);
				}

				return stack;
			}
		});

		// the hotbar
		for (int k = 0; k < 9; ++k) {
			addSlotToContainer(new Slot(player.inventory, k, 8 + (k * 18), 169));
		}
		cost = 0;
	}

	@Override
	public void addListener(IContainerListener listener) {
		super.addListener(listener);
		listener.sendWindowProperty(this, 0, cost);
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return playerIn.getDistanceSq(selfPosition.getX() + 0.5D, selfPosition.getY() + 0.5D,
				selfPosition.getZ() + 0.5D) <= 16.0D;
	}

	/**
	 * Called when the container is closed.
	 */
	@Override
	public void onContainerClosed(EntityPlayer playerIn) {
		super.onContainerClosed(playerIn);

		if (!world.isRemote) {
			clearContainer(playerIn, world, inputSlots);
		}
	}

	/**
	 * Callback for when the crafting matrix is changed.
	 */
	@Override
	public void onCraftMatrixChanged(IInventory inventoryIn) {
		super.onCraftMatrixChanged(inventoryIn);

		if (inventoryIn == inputSlots) {
			updateSchematicOutput();
		}
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

			if (index == 2) {
				if (!mergeItemStack(itemstack1, 3, 39, true)) {
					return ItemStack.EMPTY;
				}

				slot.onSlotChange(itemstack1, itemstack);
			} else if ((index != 0) && (index != 1)) {
				if ((index >= 3) && (index < 39) && !mergeItemStack(itemstack1, 0, 2, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!mergeItemStack(itemstack1, 3, 39, false)) {
				return ItemStack.EMPTY;
			}

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

	/**
	 * used by the Anvil GUI to update the Item Name being typed by the player
	 */
	public void updateSchematicContents(String newName, NBTTagCompound tag) {
		schematicName = newName;
		nbtTag = tag;

		updateSchematicOutput();
	}

	/**
	 * calculates the new result and puts it in the output slot
	 */
	public void updateSchematicOutput() {
		ItemStack itemstack = inputSlots.getStackInSlot(0);
		cost = 1;

		if (itemstack.isEmpty()) {
			outputSlot.setInventorySlotContents(0, ItemStack.EMPTY);
			cost = 0;
		} else {
			ItemStack itemstack1 = itemstack.copy();
			// we should change the name to the currently selected schematic, how do we do
			// that
			if (nbtTag != null) {
				nbtTag.setString("title", schematicName);
				Schematic schem = new Schematic(schematicName, nbtTag);
				itemstack1 = new ItemStack(SchematicMod.schematic);
				itemstack1.setTagCompound(nbtTag);
				cost = MathHelper.clamp(schem.getTotalMaterialCost() / 500, 1, 64);
			}
			outputSlot.setInventorySlotContents(0, itemstack1);

			detectAndSendChanges();
		}
	}
}