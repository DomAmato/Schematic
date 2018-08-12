package com.dyn.schematics;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.dyn.schematics.reference.ModConfig;
import com.dyn.schematics.utils.SimpleItemStack;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class Schematic {
	public static NBTTagCompound generateSchematicNBT(World world, BlockPos bottom, BlockPos top) {
		NBTTagCompound tagCompound = new NBTTagCompound();

		short width = (short) (1 + Math.abs(bottom.getX() - top.getX()));
		short height = (short) (1 + Math.abs(bottom.getY() - top.getY()));
		short length = (short) (1 + Math.abs(bottom.getZ() - top.getZ()));

		tagCompound.setShort("Width", width);
		tagCompound.setShort("Length", length);
		tagCompound.setShort("Height", height);

		int size = width * height * length;

		final byte[] localBlocks = new byte[size];
		final byte[] localMetadata = new byte[size];
		final byte[] extraBlocks = new byte[size];
		final byte[] extraBlocksNibble = new byte[(int) Math.ceil(size / 2.0)];
		boolean extra = false;

		final NBTTagList tileEntitiesList = new NBTTagList();

		final Map<String, Short> mappings = new HashMap<>();
		for (int y = 0; y < height; y++) {
			for (int z = 0; z < length; z++) {
				for (int x = 0; x < width; x++) {
					final int index = (y * width * length) + (z * width) + x;
					final IBlockState blockState = world.getBlockState(bottom.add(x, y, z));
					final Block block = blockState.getBlock();
					final int blockId = Block.REGISTRY.getIDForObject(block);
					localBlocks[index] = (byte) blockId;
					localMetadata[index] = (byte) block.getMetaFromState(blockState);
					if ((extraBlocks[index] = (byte) (blockId >> 8)) > 0) {
						extra = true;
					}

					final String name = String.valueOf(Block.REGISTRY.getNameForObject(block));
					if (!mappings.containsKey(name)) {
						mappings.put(name, (short) blockId);
					}

					if (blockState.getBlock() instanceof ITileEntityProvider) {
						TileEntity tile = world.getTileEntity(bottom.add(x, y, z));
						if (tile != null) {
							NBTTagCompound tag = new NBTTagCompound();
							tile.writeToNBT(tag);
							tag.setInteger("x", x);
							tag.setInteger("y", y);
							tag.setInteger("z", z);
							tileEntitiesList.appendTag(tag);
						}
					}
				}
			}
		}

		for (int i = 0; i < extraBlocksNibble.length; i++) {
			if (((i * 2) + 1) < extraBlocks.length) {
				extraBlocksNibble[i] = (byte) ((extraBlocks[(i * 2) + 0] << 4) | extraBlocks[(i * 2) + 1]);
			} else {
				extraBlocksNibble[i] = (byte) (extraBlocks[(i * 2) + 0] << 4);
			}
		}

		final NBTTagCompound nbtMapping = new NBTTagCompound();
		for (final Map.Entry<String, Short> entry : mappings.entrySet()) {
			nbtMapping.setShort(entry.getKey(), entry.getValue());
		}

		NBTTagList entityList = new NBTTagList();

		tagCompound.setString("Materials", "Alpha");
		tagCompound.setByteArray("Blocks", localBlocks);
		tagCompound.setByteArray("Data", localMetadata);
		if (extra) {
			tagCompound.setByteArray("AddBlocks", extraBlocksNibble);
		}
		tagCompound.setTag("Entities", entityList);
		tagCompound.setTag("TileEntities", tileEntitiesList);
		tagCompound.setTag("SchematicaMapping", nbtMapping);
		return tagCompound;
	}

	public static int getCost(ItemStack stack) {
		if (stack.hasTagCompound()) {
			NBTTagCompound compound = stack.getTagCompound();
			if (compound.hasKey("Blocks") && compound.hasKey("Data") && compound.hasKey("Width")
					&& compound.hasKey("Length") && compound.hasKey("Height")) {
				Schematic schem = new Schematic(stack.getDisplayName(), compound);
				return MathHelper.clamp(schem.getTotalMaterialCost() / 500, 1, 64);

			}
		}
		return 0;
	}

	// thanks
	// https://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values-java
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
		Collections.sort(list, (o1, o2) -> -1 * (o1.getValue()).compareTo(o2.getValue()));

		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	private String name;
	private short width;
	private short height;
	private short length;
	private Map<BlockPos, NBTTagCompound> tileEntities;
	private NBTTagList entityList;

	private NBTTagList tileList;

	private short[] blockIds;

	private byte[] metadata;
	Map<SimpleItemStack, Integer> reqMaterial = Maps.newHashMap();
	private int materialCost = 0;

	public Schematic(String name) {
		this.name = name;
	}

	public Schematic(String name, NBTTagCompound compound) {
		this.name = name;
		readFromNBT(compound);
	}

	public void build(World world, BlockPos start, int rotation, EnumFacing facing, ICommandSender sender,
			final boolean replaceAir) {
		if ((world == null) || (start == null)) {
			return;
		}

		// the translation values are length for E/W and width for N/S on E/W
		// sides and the inverse for the N/S sides
		switch (facing) {
		case EAST:
			rotation++;
			if ((rotation % 2) == 1) {
				start = start.west(length);
			} else {
				start = start.west(width);
			}
			break;
		case NORTH:
			break;
		case SOUTH:
			rotation += 2;
			if ((rotation % 2) == 1) {
				start = start.west(length).north(width);
			} else {
				start = start.west(width).north(length);
			}
			break;
		case WEST:
			rotation += 3;
			if ((rotation % 2) == 1) {
				start = start.north(width);
			} else {
				start = start.north(length);
			}
			break;
		default:
			break;
		}

		rotation %= 4;

		if (getSize() < 100000) {
			sender.sendMessage(new TextComponentString("Building Schematic: " + name));
			for (int i = 0; i < getSize(); i++) {
				int x = i % width;
				int z = ((i - x) / width) % length;
				int y = (((i - x) / width) - z) / length;
				place(world, start, rotation, x, y, z, true, replaceAir);

			}

			for (int i = 0; i < getSize(); i++) {
				int x = (i) % width;
				int z = (((i) - x) / width) % length;
				int y = ((((i) - x) / width) - z) / length;
				place(world, start, rotation, x, y, z, false, replaceAir);
			}
		} else {
			sender.sendMessage(new TextComponentString("Building Schematic: " + name));
			Queue<BlockPos> buildQueue = new LinkedList<>();
			for (int i = 0; i < getSize(); i++) {
				int x = (i) % width;
				int z = (((i) - x) / width) % length;
				int y = ((((i) - x) / width) - z) / length;
				buildQueue.add(new BlockPos(x, y, z));
			}
			for (int i = 0; i < getSize(); i++) {
				int x = (i) % width;
				int z = (((i) - x) / width) % length;
				int y = ((((i) - x) / width) - z) / length;
				buildQueue.add(new BlockPos(x, y, z));
			}
			final int immutable_rotation = rotation;
			final BlockPos immutable_pos = start;
			final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
			executor.scheduleWithFixedDelay(() -> {
				sender.sendMessage(new TextComponentString(
						"Progress: " + (int) (100 * (1 - (buildQueue.size() / (float) (getSize() * 2.0)))) + "%"));
				if (buildQueue.isEmpty()) {
					executor.shutdown();
				} else if (buildQueue.size() > getSize()) {
					for (int i = 0; (i < 25000) && (buildQueue.size() >= getSize()); i++) {
						BlockPos pos = buildQueue.poll();
						place(world, immutable_pos, immutable_rotation, pos.getX(), pos.getY(), pos.getZ(), true,
								replaceAir);
					}
				} else {
					for (int i = 0; (i < 25000) && !buildQueue.isEmpty(); i++) {
						BlockPos pos = buildQueue.poll();
						place(world, immutable_pos, immutable_rotation, pos.getX(), pos.getY(), pos.getZ(), false,
								replaceAir);
					}
				}
			}, 500, 500, TimeUnit.MILLISECONDS);
		}
	}

	public short getBlockIdAt(BlockPos pos) {
		return getBlockIdAt(pos.getX(), pos.getX(), pos.getX());
	}

	public short getBlockIdAt(int x, int y, int z) {
		return getBlockIdAtIndex(xyzToIndex(x, y, z));
	}

	public short getBlockIdAtIndex(int i) {
		return blockIds[i];
	}

	public short getBlockMetadataAt(BlockPos pos) {
		return getBlockMetadataAt(pos.getX(), pos.getX(), pos.getX());
	}

	public short getBlockMetadataAt(int x, int y, int z) {
		return getBlockMetadataAtIndex(xyzToIndex(x, y, z));
	}

	public short getBlockMetadataAtIndex(int i) {
		return metadata[i];
	}

	/**
	 * @return the entityList
	 */
	public NBTTagList getEntityList() {
		return entityList;
	}

	/**
	 * @return the height
	 */
	public short getHeight() {
		return height;
	}

	/**
	 * @return the length
	 */
	public short getLength() {
		return length;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	public Map<SimpleItemStack, Integer> getRequiredMaterials() {
		return reqMaterial;
	}

	public int getSize() {
		return width * height * length;
	}

	/**
	 * @return the tileEntities
	 */
	public Map<BlockPos, NBTTagCompound> getTileEntities() {
		return tileEntities;
	}

	public NBTTagCompound getTileEntityTag(int x, int y, int z, BlockPos pos) {
		NBTTagCompound tag = tileEntities.get(new BlockPos(x, y, z));
		tag.setInteger("x", pos.getX());
		tag.setInteger("y", pos.getY());
		tag.setInteger("z", pos.getZ());
		return tag;
	}

	/**
	 * @return the tileList
	 */
	public NBTTagList getTileList() {
		return tileList;
	}

	public int getTotalMaterialCost() {
		return materialCost;
	}

	/**
	 * @return the width
	 */
	public short getWidth() {
		return width;
	}

	// we need to go over it twice because things like torches and other blocks
	// wont place right
	public void place(World world, BlockPos start, int rotation, int x, int y, int z, boolean flag,
			boolean replaceAir) {
		int i = xyzToIndex(x, y, z);

		Block b = Block.getBlockById(blockIds[i]);
		if ((b == null) || (flag && !b.getDefaultState().isFullBlock() && (b != Blocks.AIR))
				|| (!flag && (b.getDefaultState().isFullBlock() || (b == Blocks.AIR)))
				|| (!replaceAir && (b == Blocks.AIR))) {
			return;
		}

		if (Math.abs(rotation) > 3) {
			if (rotation > 0) {
				rotation = (rotation / 90) % 4;
			} else {
				rotation = 4 - ((Math.abs(rotation) / 90) % 4);
			}
		}

		BlockPos pos = start.add(rotatePos(x, y, z, rotation));
		IBlockState state = b.getStateFromMeta(metadata[i]);
		state = rotationState(state, rotation);
		world.setBlockState(pos, state, 3);
		if (b instanceof ITileEntityProvider) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile != null) {
				NBTTagCompound comp = getTileEntityTag(x, y, z, pos);
				if (comp != null) {
					tile.readFromNBT(comp);
					tile.markDirty();
				}
			}
		}
	}

	public void readFromNBT(final NBTTagCompound tagCompound) {
		final byte[] localBlocks = tagCompound.getByteArray("Blocks");
		final byte[] localMetadata = tagCompound.getByteArray("Data");

		boolean extra = false;
		byte extraBlocks[] = null;
		byte extraBlocksNibble[] = null;
		if (tagCompound.hasKey("AddBlocks")) {
			extra = true;
			extraBlocksNibble = tagCompound.getByteArray("AddBlocks");
			extraBlocks = new byte[extraBlocksNibble.length * 2];
			for (int i = 0; i < extraBlocksNibble.length; i++) {
				extraBlocks[(i * 2) + 0] = (byte) ((extraBlocksNibble[i] >> 4) & 0xF);
				extraBlocks[(i * 2) + 1] = (byte) (extraBlocksNibble[i] & 0xF);
			}
		} else if (tagCompound.hasKey("Add")) {
			extra = true;
			extraBlocks = tagCompound.getByteArray("Add");
		}

		width = tagCompound.getShort("Width");
		length = tagCompound.getShort("Length");
		height = tagCompound.getShort("Height");

		Short id = null;
		final Map<Short, Short> oldToNew = new HashMap<>();
		if (tagCompound.hasKey("SchematicaMapping")) {
			final NBTTagCompound mapping = tagCompound.getCompoundTag("SchematicaMapping");
			final Set<String> names = mapping.getKeySet();
			for (final String name : names) {
				oldToNew.put(mapping.getShort(name),
						(short) Block.REGISTRY.getIDForObject(Block.REGISTRY.getObject(new ResourceLocation(name))));
			}
		}

		blockIds = new short[localBlocks.length];
		metadata = new byte[localBlocks.length];
		for (int y = 0; y < height; y++) {
			for (int z = 0; z < length; z++) {
				for (int x = 0; x < width; x++) {
					final int index = x + (((y * length) + z) * width);
					short blockID = (short) ((localBlocks[index] & 0xFF)
							| (extra ? ((extraBlocks[index] & 0xFF) << 8) : 0));
					final byte meta = (byte) (localMetadata[index] & 0xFF);

					if ((id = oldToNew.get(blockID)) != null) {
						blockID = id;
					}

					blockIds[xyzToIndex(x, y, z)] = blockID;
					metadata[xyzToIndex(x, y, z)] = meta;
				}
			}
		}

		entityList = tagCompound.getTagList("Entities", Constants.NBT.TAG_COMPOUND);

		tileEntities = new HashMap<>();
		tileList = tagCompound.getTagList("TileEntities", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < tileList.tagCount(); ++i) {
			NBTTagCompound teTag = tileList.getCompoundTagAt(i);
			int x = teTag.getInteger("x");
			int y = teTag.getInteger("y");
			int z = teTag.getInteger("z");
			tileEntities.put(new BlockPos(x, y, z), teTag);
		}

		Random rand = new Random();
		for (int i = 0; i < blockIds.length; i++) {
			Block b = Block.getBlockById(blockIds[i]);
			if ((b == null) || (b == Blocks.AIR) || (b == Blocks.BARRIER) || (b instanceof BlockLeaves)
					|| (b == Blocks.BEDROCK) || (b instanceof BlockTallGrass) || (b instanceof BlockDoublePlant)
					|| (b instanceof BlockSkull) || (b == Blocks.MOB_SPAWNER)) {
				continue;
			}

			if ((b == Blocks.GRASS) || (b == Blocks.GRASS_PATH)) {
				b = Blocks.DIRT;
			}

			int meta = metadata[i];

			int amount = 1;
			ItemStack stack = new ItemStack(b, 1, meta);

			if (stack.isEmpty() || (b instanceof BlockSlab)) {
				IBlockState state = b.getStateFromMeta(meta);
				stack = new ItemStack(b.getItemDropped(state, rand, 0), 1, b.damageDropped(state));
				// this likely means the material can't be placed in an inventory
				amount = b.quantityDropped(rand);
				if (stack.isEmpty()) {
					continue;
				}
			}

			if (!ModConfig.getConfig().req_exact || !stack.getHasSubtypes()) {
				stack.setItemDamage(0);
			}
			SimpleItemStack key = new SimpleItemStack(stack);

			if (reqMaterial.containsKey(key)) {
				reqMaterial.replace(key, reqMaterial.get(key) + amount);
			} else {
				reqMaterial.put(key, amount);
			}
		}

		reqMaterial = Schematic.sortByValue(reqMaterial);

		materialCost = 0;
		for (Entry<SimpleItemStack, Integer> material : reqMaterial.entrySet()) {
			materialCost += material.getValue();
		}
	}

	public BlockPos rotatePos(int x, int y, int z, int rotation) {
		if (rotation == 1) {
			return new BlockPos(length - z - 1, y, x);
		}
		if (rotation == 2) {
			return new BlockPos(width - x - 1, y, length - z - 1);
		}
		if (rotation == 3) {
			return new BlockPos(z, y, width - x - 1);
		}
		return new BlockPos(x, y, z);
	}

	public IBlockState rotationState(IBlockState state, int rotation) {
		if (rotation == 0) {
			return state;
		}
		Set<IProperty<?>> set = state.getProperties().keySet();
		for (IProperty prop : set) {
			if (!(prop instanceof PropertyDirection)) {
				continue;
			}
			EnumFacing direction = (EnumFacing) state.getValue(prop);
			if (direction == EnumFacing.UP) {
				continue;
			}
			if (direction == EnumFacing.DOWN) {
				continue;
			}
			for (int i = 0; i < rotation; ++i) {
				direction = direction.rotateY();
			}
			return state.withProperty(prop, direction);
		}
		return state;
	}

	public NBTTagCompound writeToNBT(final NBTTagCompound tagCompound) {
		tagCompound.setShort("Width", getWidth());
		tagCompound.setShort("Length", getLength());
		tagCompound.setShort("Height", getHeight());

		final byte[] localBlocks = new byte[getSize()];
		final byte[] localMetadata = new byte[getSize()];
		final byte[] extraBlocks = new byte[getSize()];
		final byte[] extraBlocksNibble = new byte[(int) Math.ceil(getSize() / 2.0)];
		boolean extra = false;

		final Map<String, Short> mappings = new HashMap<>();
		for (int y = 0; y < height; y++) {
			for (int z = 0; z < length; z++) {
				for (int x = 0; x < width; x++) {
					final int index = xyzToIndex(x, y, z);
					final Block block = Block.getBlockById(blockIds[index]);
					final int blockId = Block.REGISTRY.getIDForObject(block);
					localBlocks[index] = (byte) blockId;
					localMetadata[index] = metadata[index];
					if ((extraBlocks[index] = (byte) (blockId >> 8)) > 0) {
						extra = true;
					}

					final String name = String.valueOf(Block.REGISTRY.getNameForObject(block));
					if (!mappings.containsKey(name)) {
						mappings.put(name, (short) blockId);
					}
				}
			}
		}

		final NBTTagList tileEntitiesList = new NBTTagList();
		for (Entry<BlockPos, NBTTagCompound> entry : getTileEntities().entrySet()) {
			tileEntitiesList.appendTag(entry.getValue());
		}

		for (int i = 0; i < extraBlocksNibble.length; i++) {
			if (((i * 2) + 1) < extraBlocks.length) {
				extraBlocksNibble[i] = (byte) ((extraBlocks[(i * 2) + 0] << 4) | extraBlocks[(i * 2) + 1]);
			} else {
				extraBlocksNibble[i] = (byte) (extraBlocks[(i * 2) + 0] << 4);
			}
		}

		final NBTTagCompound nbtMapping = new NBTTagCompound();
		for (final Map.Entry<String, Short> entry : mappings.entrySet()) {
			nbtMapping.setShort(entry.getKey(), entry.getValue());
		}

		tagCompound.setString("Materials", "Alpha");
		tagCompound.setByteArray("Blocks", localBlocks);
		tagCompound.setByteArray("Data", localMetadata);
		if (extra) {
			tagCompound.setByteArray("AddBlocks", extraBlocksNibble);
		}
		tagCompound.setTag("Entities", entityList);
		tagCompound.setTag("TileEntities", tileEntitiesList);
		tagCompound.setTag("SchematicaMapping", nbtMapping);

		return tagCompound;
	}

	public int xyzToIndex(int x, int y, int z) {
		return (((y * length) + z) * width) + x;
	}
}
