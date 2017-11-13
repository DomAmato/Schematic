package com.dyn.schematics.commands;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Pattern;

import com.dyn.schematics.item.ItemSchematic;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CommandSaveSchematic extends CommandBase {

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
		BlockPos pos1 = BlockPos.ORIGIN;
		try {
			pos1 = CommandBase.parseBlockPos(sender, args, 0, false);
		} catch (NumberInvalidException e) {
			throw new CommandException("Location should be in numbers", new Object[0]);
		}

		BlockPos pos2 = BlockPos.ORIGIN;
		try {
			pos2 = CommandBase.parseBlockPos(sender, args, 3, false);
		} catch (NumberInvalidException e) {
			throw new CommandException("Location should be in numbers", new Object[0]);
		}

		String name = sender.getName() + "_" + (System.currentTimeMillis() / 1000) + ".schematic";
		if (args.length > 6) {
			name = args[6] + ".schematic";
		}

		BlockPos bottom = getMinPoint(pos1, pos2);
		BlockPos top = getMaxPoint(pos1, pos2);

		World world = sender.getEntityWorld();
		NBTTagCompound nbt = new NBTTagCompound();

		short width = (short) (1 + Math.abs(bottom.getX() - top.getX()));
		short height = (short) (1 + Math.abs(bottom.getY() - top.getY()));
		short length = (short) (1 + Math.abs(bottom.getZ() - top.getZ()));

		nbt.setShort("Width", width);
		nbt.setShort("Height", height);
		nbt.setShort("Length", length);

		nbt.setString("Materials", "Alpha");

		NBTTagList tileEntities = new NBTTagList();

		byte[][] arr = getBlockBytes(world, width, height, length, bottom, tileEntities);
		nbt.setByteArray("Blocks", arr[0]);
		nbt.setByteArray("Data", arr[1]);
		if (arr.length > 2) {
			nbt.setByteArray("AddBlocks", arr[2]);
		}
		nbt.setTag("TileEntities", tileEntities);

		try {
			DataOutputStream dataoutputstream = new DataOutputStream(
					new FileOutputStream(new File(server.getDataDirectory(), "schematics/" + name)));
			CompressedStreamTools.writeCompressed(nbt, dataoutputstream);
		} catch (IOException e) {
			throw new CommandException("Failed writing schematic to file", new Object[0]);
		}

		ItemStack stack = CommandBase.getCommandSenderAsPlayer(sender).getHeldItemMainhand();
		if (stack.getItem() instanceof ItemSchematic) {
			stack.setTagCompound(nbt);
			stack.setStackDisplayName(name.split(Pattern.quote("."))[0]);
		} else {
			stack = CommandBase.getCommandSenderAsPlayer(sender).getHeldItemOffhand();
			if (stack.getItem() instanceof ItemSchematic) {
				nbt.setString("title", name.split(Pattern.quote("."))[0]);
				stack.setTagCompound(nbt);
			} else {
				throw new CommandException("Must have schematic item equipped", new Object[0]);
			}
		}
	}

	public byte[][] getBlockBytes(World world, int width, int height, int length, BlockPos bottom,
			NBTTagList tileEntities) {
		byte[] blocks = new byte[width * height * length];
		byte[] blocksMeta = new byte[width * height * length];
		byte[] addBlocks = null;

		for (int y = 0; y < height; y++) {
			for (int z = 0; z < length; z++) {
				for (int x = 0; x < width; x++) {
					BlockPos curPos = bottom.add(x, y, z);
					IBlockState state = world.getBlockState(curPos);
					Block block = state.getBlock();
					int id = Block.getIdFromBlock(block);
					int meta = block.getMetaFromState(state);
					int index = (y * width * length) + (z * width) + x;
					if (id > 255) {
						if (addBlocks == null) {
							addBlocks = new byte[(blocks.length >> 1) + 1];
						}

						if ((index & 0x1) == 0x0) {
							addBlocks[index >> 1] = (byte) ((addBlocks[index >> 1] & 0xF0) | ((id >> 8) & 0xF));
						} else {
							addBlocks[index >> 1] = (byte) ((addBlocks[index >> 1] & 0xF) | (((id >> 8) & 0xF) << 4));
						}
					}
					blocks[index] = (byte) id;
					blocksMeta[index] = (byte) meta;

					if (state.getBlock() instanceof ITileEntityProvider) {
						TileEntity tile = world.getTileEntity(curPos);
						if (tile != null) {
							NBTTagCompound tag = new NBTTagCompound();
							tile.writeToNBT(tag);
							tag.setInteger("x", x);
							tag.setInteger("y", y);
							tag.setInteger("z", z);
							tileEntities.appendTag(tag);
						}
					}
				}
			}
		}

		if (addBlocks == null) {
			return new byte[][] { blocks, blocksMeta };
		}
		return new byte[][] { blocks, blocksMeta, addBlocks };
	}

	/**
	 * Get the highest XYZ coordinate in OOBB [p1,p2]
	 */
	private BlockPos getMaxPoint(BlockPos p1, BlockPos p2) {
		return new BlockPos(Math.max(p1.getX(), p2.getX()), Math.max(p1.getY(), p2.getY()),
				Math.max(p1.getZ(), p2.getZ()));
	}

	/**
	 * Get the lowest XYZ coordinate in OOBB [p1,p2]
	 */
	private BlockPos getMinPoint(BlockPos p1, BlockPos p2) {
		return new BlockPos(Math.min(p1.getX(), p2.getX()), Math.min(p1.getY(), p2.getY()),
				Math.min(p1.getZ(), p2.getZ()));
	}

	@Override
	public String getName() {
		return "saveschematic";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/saveschematic <x> <y> <z> <x2> <y2> <z2> [name]";
	}
}
