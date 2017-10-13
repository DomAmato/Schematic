package com.dyn.schematics;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class SchematicsTab extends CreativeTabs {

	public SchematicsTab() {
		super("schems");
	}

	@Override
	public ItemStack getTabIconItem() {
		return new ItemStack(SchematicMod.schematic);
	}

}
