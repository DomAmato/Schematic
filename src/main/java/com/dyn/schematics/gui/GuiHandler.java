package com.dyn.schematics.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == GuiArchitect.ID) {
			return new GuiArchitect(player, world);
		}
		if (ID == GuiClaimBlock.ID) {
			return new GuiClaimBlock(player, world, new BlockPos(x, y, z));
		}
		return null;
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == GuiArchitect.ID) {
			return new ContainerArchitect(player, world, new BlockPos(x, y, z));
		}
		if (ID == GuiClaimBlock.ID) {
			return new ContainerClaimBlock(player, world, new BlockPos(x, y, z));
		}
		return null;
	}
}
