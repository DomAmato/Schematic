package com.dyn.schematics.gui;

import java.util.List;
import java.util.Map.Entry;

import com.dyn.schematics.Schematic;
import com.dyn.schematics.SchematicMod;
import com.dyn.schematics.reference.ModConfig;
import com.dyn.schematics.registry.SchematicRegistry;
import com.dyn.schematics.utils.SimpleItemStack;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketSetSlot;
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
	private List<String> schem_list;
	private Schematic schematic;
	private ItemStack outputStack = ItemStack.EMPTY;

	private int schem_index = 0;

	private EntityPlayer player;

	@SideOnly(Side.CLIENT)
	public ContainerArchitect(EntityPlayer player, World worldIn) {
		this(player, worldIn, BlockPos.ORIGIN);
	}

	public ContainerArchitect(EntityPlayer player, final World worldIn, final BlockPos blockPosIn) {
		outputSlot = new InventoryCraftResult();
		player.inventory.openInventory(player);
		this.player = player;
		inputSlots = new InventoryBasic("Achitect", true, 2) {

			/**
			 * Sets the given item stack to the specified slot in the inventory (can be
			 * crafting or armor sections).
			 */
			@Override
			public void setInventorySlotContents(int index, ItemStack stack) {
				super.setInventorySlotContents(index, stack);
				if (index == 0) {
					updateSchematicOutput();
				}
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
				if (Item.getByNameOrId(ModConfig.getConfig().currency) == null) {
					ModConfig.getConfig().currency = Items.GOLD_INGOT.getRegistryName().toString();
				}
				return super.isItemValid(stack)
						&& (stack.getItem() == Item.getByNameOrId(ModConfig.getConfig().currency));
			}

		});
		addSlotToContainer(new Slot(outputSlot, 0, 190, 120) {
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

				applyCost(Schematic.getCost(stack));

				return stack;
			}
		});

		// the hotbar
		for (int k = 0; k < 9; ++k) {
			addSlotToContainer(new Slot(player.inventory, k, 8 + (k * 18), 169));
		}
		cost = 0;
		schem_list = SchematicRegistry.enumerateSchematics();
	}

	@Override
	public void addListener(IContainerListener listener) {
		super.addListener(listener);
		listener.sendWindowProperty(this, 0, cost);
	}

	public void applyCost(int cost) {
		inputSlots.setInventorySlotContents(0, ItemStack.EMPTY);

		ItemStack itemstack = inputSlots.getStackInSlot(1);

		if (itemstack.getCount() > cost) {
			itemstack.shrink(cost);
			inputSlots.setInventorySlotContents(1, itemstack);
		} else {
			inputSlots.setInventorySlotContents(1, ItemStack.EMPTY);
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return playerIn.getDistanceSq(selfPosition.getX() + 0.5D, selfPosition.getY() + 0.5D,
				selfPosition.getZ() + 0.5D) <= 16.0D;
	}

	public Schematic getSchematic() {
		if (!ItemStack.areItemStacksEqualUsingNBTShareTag(outputStack, outputSlot.getStackInSlot(0))) {
			if (outputSlot.getStackInSlot(0).hasTagCompound()) {
				NBTTagCompound compound = outputSlot.getStackInSlot(0).getTagCompound();
				schematic = new Schematic(compound.getString("title"), compound);
				cost = MathHelper.clamp(schematic.getTotalMaterialCost() / 500, 1, 64);
			}
			outputStack = outputSlot.getStackInSlot(0);
		}
		return schematic;
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
				if (!mergeItemStack(itemstack1, 3, 12, true)) {
					return ItemStack.EMPTY;
				}

				slot.onSlotChange(itemstack1, itemstack);
				applyCost(cost);
			} else if ((index != 0) && (index != 1)) {
				if ((index >= 3) && (index < 12) && !mergeItemStack(itemstack1, 0, 2, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!mergeItemStack(itemstack1, 3, 12, false)) {
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
	public void updateSchematicContents(boolean next) {
		if (!schem_list.isEmpty()) {
			if (next) {
				schem_index++;
				schem_index %= schem_list.size();
				schem_index = Math.max(schem_index, 0);
			} else {
				schem_index--;
				if (schem_index < 0) {
					schem_index = Math.max(schem_list.size() - 1, 0);
				}
			}
			schematic = SchematicRegistry.load(schem_list.get(schem_index));
		}

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
			NBTTagCompound nbtTag = new NBTTagCompound();
			if (schematic == null) {
				outputSlot.setInventorySlotContents(0, new ItemStack(SchematicMod.schematic));
			} else {
				itemstack1 = new ItemStack(SchematicMod.schematic);

				nbtTag.setString("title", schematic.getName());
				nbtTag.setInteger("cost", schematic.getTotalMaterialCost());
				NBTTagList materials = new NBTTagList();
				int counter = 0;
				for (Entry<SimpleItemStack, Integer> material : schematic.getRequiredMaterials().entrySet()) {
					if (counter > 5) {
						break;
					}
					NBTTagCompound mat_tag = new NBTTagCompound();
					mat_tag.setString("name", material.getKey().getVanillStack().getDisplayName());
					mat_tag.setInteger("total", material.getValue());
					materials.appendTag(mat_tag);
					counter++;
				}
				nbtTag.setTag("com_mat", materials);
				itemstack1.setTagCompound(schematic.writeToNBT(nbtTag));
				cost = MathHelper.clamp(schematic.getTotalMaterialCost() / 500, 1, 64);
				outputSlot.setInventorySlotContents(0, itemstack1);
			}
			detectAndSendChanges();
			if (!world.isRemote) {
				((EntityPlayerMP) player).connection.sendPacket(new SPacketSetSlot(windowId, 2, itemstack1));
			}
		}
	}
}