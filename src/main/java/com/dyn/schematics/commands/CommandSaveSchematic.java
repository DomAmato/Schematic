package com.dyn.schematics.commands;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.dyn.schematics.Schematic;
import com.dyn.schematics.SchematicMod;
import com.dyn.schematics.item.ItemSchematic;
import com.dyn.schematics.network.NetworkManager;
import com.dyn.schematics.network.messages.MessageSaveSchematicToClient;
import com.dyn.schematics.reference.ModConfig;
import com.dyn.schematics.utils.SimpleItemStack;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
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

		short width = (short) (1 + Math.abs(bottom.getX() - top.getX()));
		short height = (short) (1 + Math.abs(bottom.getY() - top.getY()));
		short length = (short) (1 + Math.abs(bottom.getZ() - top.getZ()));

		int size = width * height * length;

		if (size > (ModConfig.getConfig().max_size * 2)) {
			throw new CommandException("Schematic is too large to save", new Object[0]);
		}
		World world = sender.getEntityWorld();
		NBTTagCompound nbt = Schematic.generateSchematicNBT(world, bottom, top);

		try {
			DataOutputStream dataoutputstream = new DataOutputStream(
					new FileOutputStream(new File(server.getDataDirectory(), "schematics/" + name)));
			CompressedStreamTools.writeCompressed(nbt, dataoutputstream);
			if ((sender instanceof EntityPlayer) && !SchematicMod.integrated && (size < 10000)) {
				// only send the message to save if its not integrated and the packet is less
				// than 32kb
				NetworkManager.sendTo(new MessageSaveSchematicToClient(name, nbt), (EntityPlayerMP) sender);
			}
		} catch (IOException e) {
			throw new CommandException("Failed writing schematic to file", new Object[0]);
		}

		Schematic schem = new Schematic(name.split(Pattern.quote("."))[0], nbt);
		
		ItemStack stack = CommandBase.getCommandSenderAsPlayer(sender).getHeldItemMainhand();
		if (stack.getItem() instanceof ItemSchematic) {
			nbt.setString("title", name.split(Pattern.quote("."))[0]);
			nbt.setInteger("cost", schem.getTotalMaterialCost());
			NBTTagList materials = new NBTTagList();
			int counter = 0;
			for (Entry<SimpleItemStack, Integer> material : schem.getRequiredMaterials().entrySet()) {
				if (counter > 5) {
					break;
				}
				NBTTagCompound mat_tag = new NBTTagCompound();
				mat_tag.setString("name", material.getKey().getVanillStack().getDisplayName());
				mat_tag.setInteger("total", material.getValue());
				materials.appendTag(mat_tag);
				counter++;
			}
			nbt.setTag("com_mat", materials);
			stack.setTagCompound(nbt);
		} else {
			stack = CommandBase.getCommandSenderAsPlayer(sender).getHeldItemOffhand();
			if (stack.getItem() instanceof ItemSchematic) {
				nbt.setString("title", name.split(Pattern.quote("."))[0]);
				nbt.setInteger("cost", schem.getTotalMaterialCost());
				NBTTagList materials = new NBTTagList();
				int counter = 0;
				for (Entry<SimpleItemStack, Integer> material : schem.getRequiredMaterials().entrySet()) {
					if (counter > 5) {
						break;
					}
					NBTTagCompound mat_tag = new NBTTagCompound();
					mat_tag.setString("name", material.getKey().getVanillStack().getDisplayName());
					mat_tag.setInteger("total", material.getValue());
					materials.appendTag(mat_tag);
					counter++;
				}
				nbt.setTag("com_mat", materials);
				stack.setTagCompound(nbt);
			} else {
				throw new CommandException("Must have schematic item equipped", new Object[0]);
			}
		}
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
