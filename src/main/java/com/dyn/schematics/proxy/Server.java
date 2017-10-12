package com.dyn.schematics.proxy;

import java.io.File;

import com.dyn.schematics.Schematic;
import com.dyn.schematics.registry.SchematicRegistry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.GameRegistry;

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
		return ctx.getServerHandler().playerEntity;
	}

	/**
	 * Returns the current thread based on side during message handling, used for
	 * ensuring that the message is being handled by the main thread
	 */
	@Override
	public IThreadListener getThreadFromContext(MessageContext ctx) {
		return ctx.getServerHandler().playerEntity.getServerForPlayer();
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