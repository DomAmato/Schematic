package com.dyn.schematics.proxy;

import java.io.File;

import com.dyn.schematics.Schematic;
import com.dyn.schematics.registry.SchematicRegistry;

import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class Server implements Proxy {

	@Override
	public void init() {

	}

	@Override
	public void openSchematicGui(boolean build, BlockPos pos, Schematic schem) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postInit() {
		// TODO Auto-generated method stub

	}

	@Override
	public void preInit() {
		File schematicLocs = new File(MinecraftServer.getServer().getDataDirectory(), "schematics");

		if (!schematicLocs.exists()) {
			schematicLocs.mkdir();
		}

		SchematicRegistry.addSchematicLocation(schematicLocs);
	}

	@Override
	public void registerItem(Item item, String name) {
		item.setUnlocalizedName(name);
		GameRegistry.registerItem(item, name);
	}

	@Override
	public void registerItemModels(Item item, String name, int meta) {
	}

}