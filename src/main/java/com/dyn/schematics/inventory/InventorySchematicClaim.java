package com.dyn.schematics.inventory;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.dyn.schematics.Schematic;
import com.dyn.schematics.reference.ModConfig;
import com.dyn.schematics.utils.SimpleItemStack;
import com.google.common.collect.Maps;

import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;

public class InventorySchematicClaim extends InventoryBasic {

	private Map<SimpleItemStack, Integer> totalMaterials = Maps.newHashMap();;

	public InventorySchematicClaim(Schematic schematic) {
		super(schematic.getName(), true, schematic.getRequiredMaterials().size());
		totalMaterials.putAll(schematic.getRequiredMaterials());
		totalMaterials = Schematic.sortByValue(totalMaterials);
		int index = 0;
		for (SimpleItemStack material : totalMaterials.keySet()) {
			setInventorySlotContents(index++, material.getVanillStack());
		}
	}

	@Override
	public ItemStack addItem(ItemStack itemstack) {
		if (ModConfig.getConfig().req_exact) {
			// Checks if the metadata is equal as well
			SimpleItemStack meta_key = new SimpleItemStack(itemstack);
			if(totalMaterials.containsKey(meta_key)) {
				int j = Math.min(itemstack.getCount(), totalMaterials.get(meta_key));

				if (j > 0) {
					totalMaterials.replace(meta_key, totalMaterials.get(meta_key) - j);
					itemstack.shrink(j);

					if (itemstack.isEmpty()) {
						return ItemStack.EMPTY;
					}
				}
			}
		} else {
			SimpleItemStack key = new SimpleItemStack(itemstack.getItem(), 0);
			if(totalMaterials.containsKey(key)) {
				int j = Math.min(itemstack.getCount(), totalMaterials.get(key));

				if (j > 0) {
					totalMaterials.replace(key, totalMaterials.get(key) - j);
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
