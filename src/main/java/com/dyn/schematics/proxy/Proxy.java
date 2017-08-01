package com.dyn.schematics.proxy;

import com.dyn.schematics.Schematic;

import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;

public interface Proxy {
	public void init();

	public void openSchematicGui(boolean build, BlockPos pos, Schematic schem);

	public void postInit();

	public void preInit();

	public void registerItem(Item item, String name);

	public void registerItemModels(Item item, String name, int meta);
}