package com.dyn.schematics.proxy;

import java.io.File;

import com.dyn.schematics.Schematic;
import com.dyn.schematics.registry.SchematicRegistry;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;

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
		File schematicLocs = new File(FMLCommonHandler.instance().getMinecraftServerInstance().getDataDirectory(), "schematics");

		if (!schematicLocs.exists()) {
			schematicLocs.mkdir();
		}

		SchematicRegistry.addSchematicLocation(schematicLocs);
	}
}