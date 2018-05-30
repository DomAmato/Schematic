package com.dyn.schematics.block;

import java.util.Map.Entry;
import java.util.UUID;

import com.dyn.schematics.Schematic;
import com.dyn.schematics.inventory.InventorySchematicClaim;
import com.dyn.schematics.registry.SchematicRenderingRegistry;
import com.dyn.schematics.utils.SimpleItemStack;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class ClaimBlockTileEntity extends TileEntity {

	private Schematic schematic;
	private BlockPos schem_pos;
	private boolean active;
	private int rotation;
	private UUID placer;
	private InventorySchematicClaim inventory;

	public InventorySchematicClaim getInventory() {
		return inventory;
	}

	public UUID getPlacer() {
		return placer;
	}

	public int getRotation() {
		return rotation;
	}

	public Schematic getSchematic() {
		return schematic;
	}

	public BlockPos getSchematicPos() {
		return schem_pos;
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound tagCompound = new NBTTagCompound();
		writeToNBT(tagCompound);
		return new SPacketUpdateTileEntity(pos, getBlockMetadata(), tagCompound);
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(new NBTTagCompound());
	}

	public boolean isActive() {
		return active;
	}

	public void markForUpdate() {
		world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
		markDirty();
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		super.onDataPacket(net, pkt);
		readFromNBT(pkt.getNbtCompound());
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		if (compound.hasKey("schematic")) {
			setSchematic(new Schematic(compound.getString("schem_name"), compound.getCompoundTag("schematic")),
					BlockPos.fromLong(compound.getLong("schem_loc")));

			// active = compound.getBoolean("active");
			NBTTagList nbttaglist = compound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < nbttaglist.tagCount(); i++) {
				NBTTagCompound itemtag = nbttaglist.getCompoundTagAt(i);
				int slot = itemtag.getByte("Slot") & 0xFF;
				int remaining = itemtag.getInteger("Remaining");
				SimpleItemStack sis = new SimpleItemStack(itemtag);
				inventory.setAmountRemaining(sis, remaining);
				if ((slot >= 0) && (slot < inventory.getSizeInventory())) {
					inventory.setInventorySlotContents(slot, sis.getVanillStack());
				}
			}
		}
		if (compound.hasKey("placer")) {
			placer = compound.getUniqueId("placer");
		}
		rotation = compound.getInteger("rot");
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setPlacer(UUID placer) {
		this.placer = placer;
	}

	public void setRotation(int rotation) {
		this.rotation = rotation;
	}

	public void setSchematic(Schematic schematic, BlockPos pos) {
		this.schematic = schematic;
		schem_pos = pos;
		inventory = new InventorySchematicClaim(schematic);
	}

	public void setSchematicPos(BlockPos schem_pos) {
		this.schem_pos = schem_pos;
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		if ((newState.getBlock() == Blocks.AIR) && world.isRemote) {
			SchematicRenderingRegistry.removeSchematic(schematic);
		}
		return (oldState.getBlock() != newState.getBlock());
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		if (schematic != null) {
			NBTTagCompound subcompound = new NBTTagCompound();
			schematic.writeToNBT(subcompound);
			compound.setString("schem_name", schematic.getName());
			compound.setTag("schematic", subcompound);
			compound.setLong("schem_loc", schem_pos.toLong());

			// compound.setBoolean("active", active);
			compound.setInteger("rot", rotation);
			NBTTagList nbttaglist = new NBTTagList();
			int index = 0;
			for (Entry<SimpleItemStack, Integer> material : inventory.getTotalMaterials().entrySet()) {
				NBTTagCompound itemtag = new NBTTagCompound();
				itemtag.setByte("Slot", (byte) index);
				material.getKey().writeToNBT(itemtag);
				itemtag.setInteger("Remaining", material.getValue());
				nbttaglist.appendTag(itemtag);
			}
			compound.setTag("Items", nbttaglist);
		}
		if (placer != null) {
			compound.setUniqueId("placer", placer);
		}
		return compound;
	}
}
