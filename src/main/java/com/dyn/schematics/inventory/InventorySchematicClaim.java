package com.dyn.schematics.inventory;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.dyn.schematics.Schematic;
import com.dyn.schematics.utils.SimpleItemStack;
import com.google.common.collect.Maps;

import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;

public class InventorySchematicClaim extends InventoryBasic {

	private Map<SimpleItemStack, Integer> totalMaterials = Maps.newHashMap();;

	public InventorySchematicClaim(Schematic schematic) {
		super(schematic.getName(), true, schematic.getRequiredMaterials().size());
		totalMaterials = schematic.getRequiredMaterials();
		int index = 0;
		for (SimpleItemStack material : totalMaterials.keySet()) {
			setInventorySlotContents(index++, material.getVanillStack());
		}
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
