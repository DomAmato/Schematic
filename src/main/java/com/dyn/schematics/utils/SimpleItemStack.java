package com.dyn.schematics.utils;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

public class SimpleItemStack {
	private Item item;
	private int meta;

	public SimpleItemStack(Item item, int meta) {
		this.item = item;
		this.meta = meta;
	}

	public SimpleItemStack(ItemStack stack) {
		item = stack.getItem();
		meta = stack.getMetadata();
	}

	public SimpleItemStack(NBTTagCompound compound) {
		item = compound.hasKey("id", Constants.NBT.TAG_STRING) ? Item.getByNameOrId(compound.getString("id"))
				: Items.AIR;
		meta = Math.max(0, compound.getShort("meta"));
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof SimpleItemStack) {
			return (item == ((SimpleItemStack) other).getItem()) && (meta == ((SimpleItemStack) other).getMeta());
		} else if (other instanceof ItemStack) {
			return (item == ((ItemStack) other).getItem()) && (meta == ((ItemStack) other).getMetadata());
		}
		return false;
	}

	public Item getItem() {
		return item;
	}

	public int getMeta() {
		return meta;
	}

	public ItemStack getVanillStack() {
		return new ItemStack(item, 1, meta);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((item == null) ? 0 : item.hashCode());
		result = (prime * result) + meta;
		return result;
	}

	/**
	 * Write the stack fields to a NBT object. Return the new NBT object.
	 */
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		ResourceLocation resourcelocation = Item.REGISTRY.getNameForObject(item);
		nbt.setString("id", resourcelocation == null ? "minecraft:air" : resourcelocation.toString());
		nbt.setShort("meta", (short) meta);
		return nbt;
	}
}
