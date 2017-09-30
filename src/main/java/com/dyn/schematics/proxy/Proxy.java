package com.dyn.schematics.proxy;

import com.dyn.schematics.Schematic;

import net.minecraft.util.math.BlockPos;

public interface Proxy {
	public void init();

	public void openSchematicGui(boolean build, BlockPos pos, Schematic schem);

	public void postInit();

	public void preInit();
}