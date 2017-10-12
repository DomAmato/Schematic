package com.dyn.schematics.block;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

public class BlockSchematicClaimStand extends BlockSchematicClaim {
	public static final PropertyBool CEILING = PropertyBool.create("ceiling");

	public BlockSchematicClaimStand() {
		setDefaultState(blockState.getBaseState().withProperty(BlockSchematicClaim.FACING, EnumFacing.NORTH)
				.withProperty(BlockSchematicClaimStand.CEILING, false));
	}

	@Override
	protected BlockState createBlockState() {
		return new BlockState(this, new IProperty[] { BlockSchematicClaim.FACING, BlockSchematicClaimStand.CEILING });
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	@Override
	public int getMetaFromState(IBlockState state) {
		int i = state.getValue(BlockSchematicClaim.FACING).getIndex();

		if (state.getValue(BlockSchematicClaimStand.CEILING).booleanValue()) {
			i |= 8;
		}

		return i;
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing enumfacing = EnumFacing.getFront(meta&7);
		
		return getDefaultState().withProperty(BlockSchematicClaim.FACING, enumfacing)
				.withProperty(BlockSchematicClaimStand.CEILING, Boolean.valueOf((meta & 8) > 0));
	}
}