package com.dyn.schematics.inventory;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.dyn.schematics.Schematic;
import com.dyn.schematics.utils.SimpleItemStack;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;

public class InventorySchematicClaim extends InventoryBasic {

	private Map<SimpleItemStack, Integer> totalMaterials = Maps.newHashMap();;

	public InventorySchematicClaim(String name, int slots) {
		super(name, true, slots);
	}

	@Override
	public ItemStack addItem(ItemStack itemstack) {
		for (int i = 0; i < getSizeInventory(); ++i) {
			SimpleItemStack itemstack1 = new SimpleItemStack(getStackInSlot(i));

			if (itemstack1.getItem() == itemstack.getItem()) {
				int j = Math.min(itemstack.getCount(), totalMaterials.get(itemstack1));

				if (j > 0) {
					totalMaterials.replace(itemstack1, totalMaterials.get(itemstack1) - j);
					itemstack.shrink(j);

					if (itemstack.isEmpty()) {
						return ItemStack.EMPTY;
					}
				}
			}
		}
		return itemstack;
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	public Map<SimpleItemStack, Integer> getTotalMaterials() {
		return totalMaterials;
	}

	public void loadMaterials(Schematic schem) {
		int index = 0;
		for (Entry<Block, Integer> entry : schem.getRequiredMaterials().entrySet()) {
			ItemStack stack = new ItemStack(entry.getKey());
			int amount = entry.getValue();
			Random rand = new Random();
			if (stack.isEmpty()) {
				stack = new ItemStack(entry.getKey().getItemDropped(entry.getKey().getDefaultState(), rand, 0));
				amount = amount * entry.getKey().quantityDropped(rand);
			}
			SimpleItemStack key = new SimpleItemStack(stack);
			if (totalMaterials.containsKey(key)) {
				totalMaterials.replace(key, totalMaterials.get(key) + amount);
			} else {
				setInventorySlotContents(index++, stack);
				totalMaterials.put(key, amount);
			}

		}
	}

	public void setAmountRemaining(SimpleItemStack index, int amount) {
		totalMaterials.replace(index, amount);
	}

	public void setTotalMaterials(List<Integer> amount) {
		if (amount.size() == totalMaterials.size()) {
			Map<SimpleItemStack, Integer> newTotal = Maps.newHashMap();
			int index = 0;
			for (Entry<SimpleItemStack, Integer> entry : totalMaterials.entrySet()) {
				newTotal.put(entry.getKey(), amount.get(index++));
			}
			totalMaterials = newTotal;
		}
	}
}
