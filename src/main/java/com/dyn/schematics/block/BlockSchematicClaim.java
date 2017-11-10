package com.dyn.schematics.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.dyn.schematics.SchematicMod;
import com.dyn.schematics.registry.SchematicRenderingRegistry;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockSchematicClaim extends Block implements ITileEntityProvider {

	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

	protected BlockSchematicClaim() {
		super(Material.WOOD);
	}

	/**
	 * Return true if an entity can be spawned inside the block (used to get the
	 * player's bed spawn location)
	 */
	@Override
	public boolean canSpawnInBlock() {
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new ClaimBlockTileEntity();
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		EnumFacing enumfacing = state.getValue(BlockSchematicClaim.FACING);

		switch (enumfacing) {
		case EAST:
			return new AxisAlignedBB(7F / 16F, 0.0F, 0.05F, 10.1F / 16F, 1, 0.95F);
		case WEST:
			return new AxisAlignedBB(1 - (10.1F / 16F), 0.0F, 0.05F, 1 - (7F / 16F), 1, 0.95F);
		case SOUTH:
			return new AxisAlignedBB(0.05F, 0.0F, 7F / 16F, 0.95F, 1, 10.1F / 16F);
		case NORTH:
			return new AxisAlignedBB(0.05F, 0.0F, 1 - (10.1F / 16F), 0.95F, 1, 1 - (7F / 16F));
		default:
			return new AxisAlignedBB(7F / 16F, 0.0F, 0.05F, 10.1F / 16F, 1, 0.95F);
		}
	}

	@Override
	@Nullable
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		return Block.NULL_AABB;
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		// we need this so that multiple items dont drop
		return new ArrayList<>();
	}

	/**
	 * Get the Item that this Block should drop when harvested.
	 */
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return SchematicMod.schematic;
	}

	/**
	 * Called when a user uses the creative pick block button on this block
	 *
	 * @param target
	 *            The full target the player is looking at
	 * @return A ItemStack to add to the player's inventory, Null if nothing
	 *         should be added.
	 */
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos,
			EntityPlayer player) {
		TileEntity tileentity = world.getTileEntity(pos);
		if (tileentity instanceof ClaimBlockTileEntity) {
			ItemStack is = new ItemStack(SchematicMod.schematic);
			NBTTagCompound compound = new NBTTagCompound();
			((ClaimBlockTileEntity) tileentity).getSchematic().writeToNBT(compound);
			compound.setString("title", ((ClaimBlockTileEntity) tileentity).getSchematic().getName()
					.replace("" + ((ClaimBlockTileEntity) tileentity).getSchematicPos().toLong(), ""));

			is.setTagCompound(compound);
			return is;
		} else {
			return new ItemStack(SchematicMod.schematic);
		}
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	/**
	 * Used to determine ambient occlusion and culling when rebuilding chunks
	 * for render
	 */
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
		return true;
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) {
			TileEntity tileentity = worldIn.getTileEntity(pos);
			if ((tileentity instanceof ClaimBlockTileEntity)
					&& (((ClaimBlockTileEntity) tileentity).getSchematic() != null)) {
				if (!playerIn.isSneaking()) {
					if (((ClaimBlockTileEntity) tileentity).isActive()) {
						if (SchematicRenderingRegistry.containsCompiledSchematic(
								((ClaimBlockTileEntity) tileentity).getSchematic(),
								((ClaimBlockTileEntity) tileentity).getSchematicPos())) {
							SchematicMod.proxy.openSchematicGui(true, pos,
									((ClaimBlockTileEntity) tileentity).getSchematic());
						}
					} else {
						((ClaimBlockTileEntity) tileentity).setActive(true);
					}
				}
			}
		} else {
			if (playerIn.isSneaking()) {
				TileEntity tileentity = worldIn.getTileEntity(pos);
				if ((tileentity instanceof ClaimBlockTileEntity)
						&& (((ClaimBlockTileEntity) tileentity).getSchematic() != null)) {
					((ClaimBlockTileEntity) tileentity)
							.setRotation((((ClaimBlockTileEntity) tileentity).getRotation() + 1) % 4);
					((ClaimBlockTileEntity) tileentity).markForUpdate();
				}
			}
		}
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World worldIn, BlockPos pos, Random rand) {
		TileEntity tileentity = worldIn.getTileEntity(pos);
		if ((tileentity instanceof ClaimBlockTileEntity) && (((ClaimBlockTileEntity) tileentity).getSchematic() != null)
				&& ((ClaimBlockTileEntity) tileentity).isActive()) {
			Minecraft.getMinecraft().addScheduledTask(() -> {
				SchematicRenderingRegistry.addSchematic(((ClaimBlockTileEntity) tileentity).getSchematic(),
						((ClaimBlockTileEntity) tileentity).getSchematicPos(),
						state.getValue(BlockSchematicClaim.FACING), ((ClaimBlockTileEntity) tileentity).getRotation());
			});
		}
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player,
			boolean willHarvest) {
		TileEntity tileentity = world.getTileEntity(pos);
		boolean result = super.removedByPlayer(state, world, pos, player, willHarvest);
		if (result) {
			if (!world.isRemote) {
				if (willHarvest) {
					if ((tileentity instanceof ClaimBlockTileEntity)) {
						if ((((ClaimBlockTileEntity) tileentity).getSchematic() != null)) {
							ItemStack is = new ItemStack(SchematicMod.schematic);
							NBTTagCompound compound = new NBTTagCompound();
							((ClaimBlockTileEntity) tileentity).getSchematic().writeToNBT(compound);
							compound.setString("title", ((ClaimBlockTileEntity) tileentity).getSchematic().getName()
									.replace("" + ((ClaimBlockTileEntity) tileentity).getSchematicPos().toLong(), ""));

							is.setTagCompound(compound);
							// we have to do this here otherwise it spawns in an
							// empty schematic
							Block.spawnAsEntity(world, pos, is);
						} else {
							Block.spawnAsEntity(world, pos, new ItemStack(SchematicMod.schematic));
						}
					}
				}
			} else {
				if ((tileentity instanceof ClaimBlockTileEntity)
						&& (((ClaimBlockTileEntity) tileentity).getSchematic() != null)) {
					SchematicRenderingRegistry.removeSchematic(((ClaimBlockTileEntity) tileentity).getSchematic());
				}
			}
		}
		return result;
	}
}