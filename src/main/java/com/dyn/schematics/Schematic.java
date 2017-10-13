package com.dyn.schematics;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class Schematic {
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

	public Schematic(String name) {
		this.name = name;
	}

	public Schematic(String name, NBTTagCompound compound) {
		this.name = name;

		width = compound.getShort("Width");
		height = compound.getShort("Height");
		length = compound.getShort("Length");
		byte[] addId = compound.hasKey("AddBlocks") ? compound.getByteArray("AddBlocks") : new byte[0];
		setBlockBytes(compound.getByteArray("Blocks"), addId);
		metadata = compound.getByteArray("Data");
		entityList = compound.getTagList("Entities", 10);
		tileEntities = new HashMap<>();
		tileList = compound.getTagList("TileEntities", 10);
		for (int i = 0; i < tileList.tagCount(); ++i) {
			NBTTagCompound teTag = tileList.getCompoundTagAt(i);
			int x = teTag.getInteger("x");
			int y = teTag.getInteger("y");
			int z = teTag.getInteger("z");
			tileEntities.put(new BlockPos(x, y, z), teTag);
		}
	}

	public void build(World world, BlockPos start, int rotation, EnumFacing facing, ICommandSender sender) {
		if ((world == null) || (start == null)) {
			return;
		}

		// the translation values are length for E/W and width for N/S on E/W sides
		// and the inverse for the N/S sides
		switch (facing) {
		case EAST:
			rotation++;
			start = start.west(length);
			break;
		case NORTH:
			rotation--;
			break;
		case SOUTH:
			rotation += 2;
			start = start.west(width).north(length);
			break;
		case WEST:
			rotation += 3;
			start = start.north(width);
			break;
		default:
			break;

		}

		if (getSize() < 100000) {
			sender.sendMessage(new TextComponentString("Building Schematic: " + name));
			for (int i = 0; i < getSize(); i++) {
				int x = i % width;
				int z = ((i - x) / width) % length;
				int y = (((i - x) / width) - z) / length;
				place(world, start, rotation, x, y, z, true);

			}

			for (int i = 0; i < getSize(); i++) {
				int x = (i) % width;
				int z = (((i) - x) / width) % length;
				int y = ((((i) - x) / width) - z) / length;
				place(world, start, rotation, x, y, z, false);
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
						place(world, immutable_pos, immutable_rotation, pos.getX(), pos.getY(), pos.getZ(), true);
					}
				} else {
					for (int i = 0; (i < 25000) && !buildQueue.isEmpty(); i++) {
						BlockPos pos = buildQueue.poll();
						place(world, immutable_pos, immutable_rotation, pos.getX(), pos.getY(), pos.getZ(), false);
					}
				}
			}, 500, 500, TimeUnit.MILLISECONDS);
		}
	}

	public byte[][] getBlockBytes() {
		byte[] blocks = new byte[blockIds.length];
		byte[] addBlocks = null;
		for (int i = 0; i < blocks.length; ++i) {
			short id = blockIds[i];
			if (id > 255) {
				if (addBlocks == null) {
					addBlocks = new byte[(blocks.length >> 1) + 1];
				}
				if ((i & 0x1) == 0x0) {
					addBlocks[i >> 1] = (byte) ((addBlocks[i >> 1] & 0xF0) | ((id >> 8) & 0xF));
				} else {
					addBlocks[i >> 1] = (byte) ((addBlocks[i >> 1] & 0xF) | (((id >> 8) & 0xF) << 4));
				}
			}
			blocks[i] = (byte) id;
		}
		if (addBlocks == null) {
			return new byte[][] { blocks };
		}
		return new byte[][] { blocks, addBlocks };
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

	public Map<Block, Integer> getRequiredMaterials() {
		Map<Block, Integer> reqBlocks = Maps.newHashMap();
		for (int i = 0; i < blockIds.length; i++) {
			Block b = Block.getBlockById(blockIds[i]);
			if (b == null) {
				continue;
			}
			if (b == Blocks.GRASS) {
				b = Blocks.DIRT;
			}

			int meta = metadata[i];
			IBlockState state = b.getStateFromMeta(meta);
			if (state.getBlock() == Blocks.AIR) {
				continue;
			}
			if (reqBlocks.containsKey(state.getBlock())) {
				reqBlocks.replace(state.getBlock(), reqBlocks.get(state.getBlock()) + 1);
			} else {
				reqBlocks.put(state.getBlock(), 1);
			}
		}
		return Schematic.sortByValue(reqBlocks);
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

	public int getTotalMaterialCost(Map<Block, Integer> materials) {
		int total = 0;
		for (Entry<Block, Integer> material : materials.entrySet()) {
			total += material.getValue();
		}
		return total;
	}

	/**
	 * @return the width
	 */
	public short getWidth() {
		return width;
	}

	// we need to go over it twice because things like torches and other blocks
	// wont place right
	public void place(World world, BlockPos start, int rotation, int x, int y, int z, boolean flag) {
		int i = xyzToIndex(x, y, z);
		Block b = Block.getBlockById(blockIds[i]);
		if ((b == null) || (flag && !b.isFullBlock(b.getDefaultState()) && (b != Blocks.AIR))
				|| (!flag && (b.isFullBlock(b.getDefaultState()) || (b == Blocks.AIR)))) {
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
		if (state.getBlock() instanceof ITileEntityProvider) {
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

	public void readFromNBT(NBTTagCompound compound) {
		width = compound.getShort("Width");
		height = compound.getShort("Height");
		length = compound.getShort("Length");
		byte[] addId = compound.hasKey("AddBlocks") ? compound.getByteArray("AddBlocks") : new byte[0];
		setBlockBytes(compound.getByteArray("Blocks"), addId);
		metadata = compound.getByteArray("Data");
		entityList = compound.getTagList("Entities", 10);
		tileEntities = new HashMap<>();
		tileList = compound.getTagList("TileEntities", 10);
		for (int i = 0; i < tileList.tagCount(); ++i) {
			NBTTagCompound teTag = tileList.getCompoundTagAt(i);
			int x = teTag.getInteger("x");
			int y = teTag.getInteger("y");
			int z = teTag.getInteger("z");
			tileEntities.put(new BlockPos(x, y, z), teTag);
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

	public void setBlockBytes(byte[] blockId, byte[] addId) {
		blockIds = new short[blockId.length];
		for (int index = 0; index < blockId.length; ++index) {
			short id = (short) (blockId[index] & 0xFF);
			if ((index >> 1) < addId.length) {
				if ((index & 0x1) == 0x0) {
					id += (short) ((addId[index >> 1] & 0xF) << 8);
				} else {
					id += (short) ((addId[index >> 1] & 0xF0) << 4);
				}
			}
			blockIds[index] = id;
		}
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setShort("Width", width);
		compound.setShort("Height", height);
		compound.setShort("Length", length);
		byte[][] arr = getBlockBytes();
		compound.setByteArray("Blocks", arr[0]);
		if (arr.length > 1) {
			compound.setByteArray("AddBlocks", arr[1]);
		}
		compound.setByteArray("Data", metadata);
		compound.setTag("Entities", entityList);
		compound.setTag("TileEntities", tileList);
		return compound;
	}

	public int xyzToIndex(int x, int y, int z) {
		return (((y * length) + z) * width) + x;
	}
}
