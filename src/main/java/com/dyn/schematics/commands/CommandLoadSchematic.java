package com.dyn.schematics.commands;

import com.dyn.schematics.Schematic;
import com.dyn.schematics.SchematicMod;
import com.dyn.schematics.registry.SchematicRegistry;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

public class CommandLoadSchematic extends CommandBase {

	/**
	 * Returns true if the given command sender is allowed to use this command.
	 */
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return sender.canUseCommand(getRequiredPermissionLevel(), getName())
				&& (sender.getCommandSenderEntity() instanceof EntityPlayer);
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 1) {
			throw new CommandException("Must specify name to load", new Object[0]);
		}
		Schematic schem = SchematicRegistry.load(args[0]);
		if (schem != null) {
			NBTTagCompound compound = new NBTTagCompound();
			schem.writeToNBT(compound);
			if (schem.getSize() < 600000) {
				ItemStack is = new ItemStack(SchematicMod.schematic, 1, 0);
				is.setTagCompound(compound);
				is.setStackDisplayName(schem.getName());
				EntityPlayerMP player = CommandBase.getCommandSenderAsPlayer(sender);
				player.inventory.addItemStackToInventory(is);
			} else {
				throw new CommandException("Schematic is too large to load into an item", new Object[0]);
			}
		} else {
			throw new CommandException("Could not find schematic %s", new Object[] { args[0] });
		}
	}

	@Override
	public String getName() {
		return "loadschematic";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/loadschematic <name>";
	}
}
