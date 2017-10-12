package com.dyn.schematics.commands;

import com.dyn.schematics.ItemSchematic;
import com.dyn.schematics.Schematic;
import com.dyn.schematics.registry.SchematicRegistry;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class CommandBuildSchematic extends CommandBase {

	public CommandBuildSchematic() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Returns true if the given command sender is allowed to use this command.
	 */
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return sender.canCommandSenderUseCommand(getRequiredPermissionLevel(), getCommandName())
				&& (sender.getCommandSenderEntity() instanceof EntityPlayer);
	}

	@Override
	public String getCommandName() {
		return "buildschematic";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/buildschematic <x> <y> <z> [rotation] [facing] [name|equipped schematic]";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 1) {
			throw new CommandException("Must specify a location", new Object[0]);
		}
		BlockPos pos = BlockPos.ORIGIN;
		World world = sender.getEntityWorld();
		if (args.length < 6) {
			ItemStack stack = CommandBase.getCommandSenderAsPlayer(sender).getCurrentEquippedItem();
			if (stack.getItem() instanceof ItemSchematic) {
				Schematic schem = new Schematic(stack.getDisplayName(), stack.getTagCompound());
				int rotation = 0;
				EnumFacing facing = EnumFacing.SOUTH;
				try {
					pos = CommandBase.parseBlockPos(sender, args, 0, false);
				} catch (NumberInvalidException e) {
					throw new CommandException("Location should be in numbers", new Object[0]);
				}

				if (args.length > 3) {
					try {
						rotation = Integer.parseInt(args[3]);
						if (Math.abs(rotation) > 3) {
							if (rotation > 0) {
								rotation = (rotation / 90) % 4;
							} else {
								rotation = 4 - Math.abs((rotation / 90) % 4);
							}
						}
					} catch (NumberFormatException ex) {
						throw new CommandException("Cannot Parse Rotation", new Object[0]);
					}
				}

				if (args.length == 5) {
					facing = EnumFacing.valueOf(args[4]);
				}

				schem.build(world, pos, rotation, facing, sender);
			} else {
				throw new CommandException("Must have schematic item equipped", new Object[0]);
			}
		} else {
			Schematic schem = SchematicRegistry.load(args[5]);
			if (schem == null) {
				throw new CommandException("Could not find schematic %s", new Object[] { args[4] });
			}
			int rotation = 0;
			EnumFacing facing = EnumFacing.SOUTH;
			try {
				facing = EnumFacing.valueOf(args[4]);
			} catch (Exception e) {
				throw new CommandException("Facing Direction could not be parsed", new Object[0]);
			}
			try {
				pos = CommandBase.parseBlockPos(sender, args, 0, false);
			} catch (NumberInvalidException e) {
				throw new CommandException("Location should be in numbers", new Object[0]);
			}

			if (args.length > 3) {
				try {
					rotation = Integer.parseInt(args[3]);
					if (Math.abs(rotation) > 3) {
						if (rotation > 0) {
							rotation = (rotation / 90) % 4;
						} else {
							rotation = 4 - Math.abs((rotation / 90) % 4);
						}
					}
				} catch (NumberFormatException ex) {
					throw new CommandException("Cannot Parse Rotation", new Object[0]);
				}
			}

			schem.build(world, pos, rotation, facing, sender);
		}
	}
}
