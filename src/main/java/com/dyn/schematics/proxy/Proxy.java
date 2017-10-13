package com.dyn.schematics.proxy;

import com.dyn.schematics.Schematic;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public interface Proxy {
	public void addScheduledTask(Runnable runnable);

	public EntityPlayer getPlayerEntity(MessageContext ctx);

	public IThreadListener getThreadFromContext(MessageContext ctx);

	public void init();

	public void openSchematicGui(boolean build, BlockPos pos, Schematic schem);

	public void postInit();

	public void preInit();

}