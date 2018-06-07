package com.dyn.schematics.utils;

import com.dyn.schematics.SchematicMod;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SchematicsTab extends CreativeTabs {

	public SchematicsTab() {
		super("schems");
	}

	@Override
	public ItemStack getTabIconItem() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setString("title", "placeholder");

		ItemStack is = new ItemStack(SchematicMod.schematic);
		is.setTagCompound(compound);
		return is;
	}

}
