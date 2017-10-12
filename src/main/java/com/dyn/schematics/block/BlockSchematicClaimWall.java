package com.dyn.schematics.block;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;

public class BlockSchematicClaimWall extends BlockSchematicClaim {
	public BlockSchematicClaimWall() {
		setDefaultState(blockState.getBaseState().withProperty(BlockSchematicClaim.FACING, EnumFacing.NORTH));
	}

	@Override
	protected BlockState createBlockState() {
		return new BlockState(this, new IProperty[] { BlockSchematicClaim.FACING });
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(BlockSchematicClaim.FACING).getIndex();
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing enumfacing = EnumFacing.getFront(meta&7);

		return getDefaultState().withProperty(BlockSchematicClaim.FACING, enumfacing);
	}

	@Override
	@SuppressWarnings("incomplete-switch")
	public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
		EnumFacing enumfacing = worldIn.getBlockState(pos).getValue(BlockSchematicClaim.FACING);
		float f = 0.28125F;
		float f1 = 0.78125F;
		float f2 = 0.0F;
		float f3 = 1.0F;
		float f4 = 0.125F;
		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);

		switch (enumfacing) {
		case NORTH:
			setBlockBounds(f2, f, 1.0F - f4, f3, f1, 1.0F);
			break;
		case SOUTH:
			setBlockBounds(f2, f, 0.0F, f3, f1, f4);
			break;
		case WEST:
			setBlockBounds(1.0F - f4, f, f2, 1.0F, f1, f3);
			break;
		case EAST:
			setBlockBounds(0.0F, f, f2, f4, f1, f3);
		}
	}
}