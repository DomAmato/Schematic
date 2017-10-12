package com.dyn.schematics.proxy;

import com.dyn.schematics.Schematic;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public interface Proxy {
	public void addScheduledTask(Runnable runnable);

	public EntityPlayer getPlayerEntity(MessageContext ctx);

	public IThreadListener getThreadFromContext(MessageContext ctx);

	public void init();

	public void openSchematicGui(boolean build, BlockPos pos, Schematic schem);

	public void postInit();

	public void preInit();

	public void registerItem(Item item, String name);

	public void registerItemModels(Item item, String name, int meta);

}