
package com.dyn.schematics;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;

public class Schematic {
	public static int buildSize = 10000;
	public String name;
	public short width;
	public short height;
	public short length;
	private BlockPos offset;
	private BlockPos start;
	private Map<ChunkCoordIntPair, NBTTagCompound>[] tileEntities;
	private NBTTagList entityList;
	public NBTTagList tileList;
	public short[] blockArray;
	public byte[] blockDataArray;
	private World world;
	public boolean isBuilding;
	public boolean firstLayer;
	public int buildPos;
	public int size;
	private int rotation;

	public Schematic(String name) {
		offset = BlockPos.ORIGIN;
		start = BlockPos.ORIGIN;
		isBuilding = false;
		firstLayer = true;
		rotation = 0;
		this.name = name;
	}

	public Schematic(String name, NBTTagCompound compound) {
		offset = BlockPos.ORIGIN;
		start = BlockPos.ORIGIN;
		isBuilding = false;
		firstLayer = true;
		rotation = 0;
		this.name = name;

		width = compound.getShort("Width");
		height = compound.getShort("Height");
		length = compound.getShort("Length");
		size = width * height * length;
		byte[] addId = compound.hasKey("AddBlocks") ? compound.getByteArray("AddBlocks") : new byte[0];
		setBlockBytes(compound.getByteArray("Blocks"), addId);
		blockDataArray = compound.getByteArray("Data");
		entityList = compound.getTagList("Entities", 10);
		tileEntities = new Map[height];
		tileList = compound.getTagList("TileEntities", 10);
		for (int i = 0; i < tileList.tagCount(); ++i) {
			NBTTagCompound teTag = tileList.getCompoundTagAt(i);
			int x = teTag.getInteger("x");
			int y = teTag.getInteger("y");
			int z = teTag.getInteger("z");
			Map<ChunkCoordIntPair, NBTTagCompound> map = tileEntities[y];
			if (map == null) {
				map = (tileEntities[y] = new HashMap<>());
			}
			map.put(new ChunkCoordIntPair(x, z), teTag);
		}
	}

	public void build() {
		if ((world == null) || !isBuilding) {
			return;
		}
		long endPos = buildPos + 10000;
		if (endPos > size) {
			endPos = size;
		}
		while (buildPos < endPos) {
			int x = buildPos % width;
			int z = ((buildPos - x) / width) % length;
			int y = (((buildPos - x) / width) - z) / length;
			if (firstLayer) {
				place(x, y, z, 1);
			} else {
				place(x, y, z, 2);
			}
			++buildPos;
		}
		if (buildPos >= size) {
			if (firstLayer) {
				firstLayer = false;
				buildPos = 0;
			} else {
				isBuilding = false;
			}
		}
	}

	public byte[][] getBlockBytes() {
		byte[] blocks = new byte[blockArray.length];
		byte[] addBlocks = null;
		for (int i = 0; i < blocks.length; ++i) {
			short id = blockArray[i];
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

	public Map<Block, Integer> getMaterialCosts() {
		Map<Block, Integer> reqBlocks = Maps.newHashMap();
		for (int i = 0; i < blockArray.length; i++) {
			Block b = Block.getBlockById(blockArray[i]);
			if (b == null) {
				continue;
			}
			if (b == Blocks.grass) {
				b = Blocks.dirt;
			}

			int meta = blockDataArray[i];
			IBlockState state = b.getStateFromMeta(meta);
			if (state.getBlock() == Blocks.air) {
				continue;
			}
			state = rotationState(state, rotation);
			if (reqBlocks.containsKey(state.getBlock())) {
				reqBlocks.replace(state.getBlock(), reqBlocks.get(state.getBlock()) + 1);
			} else {
				reqBlocks.put(state.getBlock(), 1);
			}
		}
		return reqBlocks;
	}

	public NBTTagCompound getNBTSmall() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setShort("Width", width);
		compound.setShort("Height", height);
		compound.setShort("Length", length);
		compound.setString("SchematicName", name);
		if (size < 125000) {
			byte[][] arr = getBlockBytes();
			compound.setByteArray("Blocks", arr[0]);
			if (arr.length > 1) {
				compound.setByteArray("AddBlocks", arr[1]);
			}
			compound.setByteArray("Data", blockDataArray);
		}
		return compound;
	}

	public int getPercentage() {
		double l = buildPos + (firstLayer ? 0 : size);
		return (int) ((l / size) * 50.0);
	}

	public NBTTagCompound getTileEntity(int x, int y, int z, BlockPos pos) {
		if ((y >= tileEntities.length) || (tileEntities[y] == null)) {
			return null;
		}
		NBTTagCompound compound = tileEntities[y].get(new ChunkCoordIntPair(x, z));
		if (compound == null) {
			return null;
		}
		compound = (NBTTagCompound) compound.copy();
		compound.setInteger("x", pos.getX());
		compound.setInteger("y", pos.getY());
		compound.setInteger("z", pos.getZ());
		return compound;
	}

	public void init(BlockPos pos, World world, int rotation) {
		start = pos;
		this.world = world;
		this.rotation = rotation;
	}

	public void load(NBTTagCompound compound) {
		width = compound.getShort("Width");
		height = compound.getShort("Height");
		length = compound.getShort("Length");
		size = width * height * length;
		byte[] addId = compound.hasKey("AddBlocks") ? compound.getByteArray("AddBlocks") : new byte[0];
		setBlockBytes(compound.getByteArray("Blocks"), addId);
		blockDataArray = compound.getByteArray("Data");
		entityList = compound.getTagList("Entities", 10);
		tileEntities = new Map[height];
		tileList = compound.getTagList("TileEntities", 10);
		for (int i = 0; i < tileList.tagCount(); ++i) {
			NBTTagCompound teTag = tileList.getCompoundTagAt(i);
			int x = teTag.getInteger("x");
			int y = teTag.getInteger("y");
			int z = teTag.getInteger("z");
			Map<ChunkCoordIntPair, NBTTagCompound> map = tileEntities[y];
			if (map == null) {
				map = (tileEntities[y] = new HashMap<>());
			}
			map.put(new ChunkCoordIntPair(x, z), teTag);
		}
	}

	public void offset(int x, int y, int z) {
		offset = new BlockPos(x, y, z);
	}

	public void place(int x, int y, int z, int flag) {
		int i = xyzToIndex(x, y, z);
		Block b = Block.getBlockById(blockArray[i]);
		if ((b == null) || ((flag == 1) && !b.isFullBlock() && (b != Blocks.air))
				|| ((flag == 2) && (b.isFullBlock() || (b == Blocks.air)))) {
			return;
		}
		int rotation = this.rotation / 90;
		BlockPos pos = start.add(rotatePos(x, y, z, rotation));
		IBlockState state = b.getStateFromMeta(blockDataArray[i]);
		state = rotationState(state, rotation);
		world.setBlockState(pos, state, 2);
		if (state.getBlock() instanceof ITileEntityProvider) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile != null) {
				NBTTagCompound comp = getTileEntity(x, y, z, pos);
				if (comp != null) {
					tile.readFromNBT(comp);
				}
			}
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
		Set<IProperty> set = state.getProperties().keySet();
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

	public NBTTagCompound save() {
		NBTTagCompound compound = new NBTTagCompound();
		return writeToNBT(compound);
	}

	public void setBlockBytes(byte[] blockId, byte[] addId) {
		blockArray = new short[blockId.length];
		for (int index = 0; index < blockId.length; ++index) {
			short id = (short) (blockId[index] & 0xFF);
			if ((index >> 1) < addId.length) {
				if ((index & 0x1) == 0x0) {
					id += (short) ((addId[index >> 1] & 0xF) << 8);
				} else {
					id += (short) ((addId[index >> 1] & 0xF0) << 4);
				}
			}
			blockArray[index] = id;
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
		compound.setByteArray("Data", blockDataArray);
		compound.setTag("Entities", entityList);
		compound.setTag("TileEntities", tileList);
		return compound;
	}

	public int xyzToIndex(int x, int y, int z) {
		return (((y * length) + z) * width) + x;
	}
}
