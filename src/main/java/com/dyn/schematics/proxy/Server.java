package com.dyn.schematics.proxy;

import java.io.File;

import com.dyn.schematics.Schematic;
import com.dyn.schematics.registry.SchematicRegistry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class Server implements Proxy {

	@Override
	public void addScheduledTask(Runnable runnable) {
		FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(runnable);
	}

	/**
	 * Returns a side-appropriate EntityPlayer for use during message handling
	 */
	@Override
	public EntityPlayer getPlayerEntity(MessageContext ctx) {
		return ctx.getServerHandler().player;
	}

	/**
	 * Returns the current thread based on side during message handling, used for
	 * ensuring that the message is being handled by the main thread
	 */
	@Override
	public IThreadListener getThreadFromContext(MessageContext ctx) {
		return ctx.getServerHandler().player.getServer();
	}

	@Override
	public void init() {

	}

	@Override
	public void openSchematicGui(boolean build, BlockPos pos, Schematic schem) {
	}

	@Override
	public void postInit() {
		// TODO Auto-generated method stub

	}

	@Override
	public void preInit() {
		File schematicLocs = new File(FMLCommonHandler.instance().getMinecraftServerInstance().getDataDirectory(),
				"schematics");

		if (!schematicLocs.exists()) {
			schematicLocs.mkdir();
		}

		SchematicRegistry.addSchematicLocation(schematicLocs);
	}

}