package com.dyn.schematics.commands;

import com.dyn.schematics.ItemSchematic;
import com.dyn.schematics.Schematic;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class CommandBuildSchematic extends CommandBase {

	public CommandBuildSchematic() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getCommandName() {
		// TODO Auto-generated method stub
		return "buildschematic";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		// TODO Auto-generated method stub
		return "/buildschematic <x> <y> <z> [rotation] must have schematic item equipped";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		BlockPos pos = BlockPos.ORIGIN;
		World world = sender.getEntityWorld();
		ItemStack stack =  getCommandSenderAsPlayer(sender).getCurrentEquippedItem();
		if(stack.getItem() instanceof ItemSchematic){
			Schematic schem = new Schematic(stack.getDisplayName(), stack.getTagCompound());
			int rotation = 0;
			try {
				pos = CommandBase.parseBlockPos(sender, args, 0, false);
			} catch (NumberInvalidException e) {
				throw new CommandException("Location should be in numbers", new Object[0]);
			}

			if (args.length > 3) {
				try {
					rotation = Integer.parseInt(args[3]);
					if(Math.abs(rotation) > 3){
						if(rotation > 0){
							rotation = (rotation/90)%4;
						} else {
							rotation = 4-Math.abs((rotation/90)%4);
						}
					}
				} catch (NumberFormatException ex) {
					throw new CommandException("Cannot Parse Rotation", new Object[0]);
				}
			}
			
			schem.build(world, pos, rotation);
		} else {
			throw new CommandException("Must have schematic item equipped", new Object[0]);
		}
	}

	/**
     * Returns true if the given command sender is allowed to use this command.
     */
    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        return sender.canCommandSenderUseCommand(this.getRequiredPermissionLevel(), this.getCommandName()) && sender.getCommandSenderEntity() instanceof EntityPlayer;
    }	
}
