package com.dyn.schematics.block;

import com.dyn.schematics.reference.Reference;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockSchematicClaimWall extends BlockSchematicClaim {
	public BlockSchematicClaimWall() {
		setRegistryName(Reference.MOD_ID, "schem_block_wall");
		setUnlocalizedName("schem_block_wall");
		setDefaultState(blockState.getBaseState().withProperty(BlockHorizontal.FACING, EnumFacing.NORTH));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { BlockHorizontal.FACING });
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		EnumFacing enumfacing = state.getValue(BlockHorizontal.FACING);
		float f = 0.28125F;
		float f1 = 0.78125F;
		float f2 = 0.0F;
		float f3 = 1.0F;
		float f4 = 0.125F;

		switch (enumfacing) {
		case NORTH:
			return new AxisAlignedBB(f2, f, 1.0F - f4, f3, f1, 1.0F);
		case SOUTH:
			return new AxisAlignedBB(f2, f, 0.0F, f3, f1, f4);
		case WEST:
			return new AxisAlignedBB(1.0F - f4, f, f2, 1.0F, f1, f3);
		case EAST:
			return new AxisAlignedBB(0.0F, f, f2, f4, f1, f3);
		default:
			return new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		}
	}

	/**
	 * Convert the BlockStateContainer into the correct metadata value
	 */
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(BlockHorizontal.FACING).getHorizontalIndex();
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer) {
		return getDefaultState().withProperty(BlockHorizontal.FACING, placer.getHorizontalFacing());
	}

	/**
	 * Convert the given metadata into a BlockStateContainer for this Block
	 */
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.getHorizontal(meta));
	}
}