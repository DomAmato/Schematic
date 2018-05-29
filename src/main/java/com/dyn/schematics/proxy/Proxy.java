package com.dyn.schematics.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public interface Proxy {
	public void addScheduledTask(Runnable runnable);

	public EntityPlayer getPlayerEntity(MessageContext ctx);

	public IThreadListener getThreadFromContext(MessageContext ctx);

	public void init();

	public void openSchematicGui();

	public void postInit();

	public void preInit();

}