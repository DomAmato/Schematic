package com.dyn.schematics;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class SchematicsTab extends CreativeTabs {

	public SchematicsTab() {
		super("schems");
	}

	@Override
	public Item getTabIconItem() {
		return SchematicMod.schematic;
	}

}
