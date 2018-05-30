package com.dyn.schematics.block;

import com.dyn.schematics.reference.Reference;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

public class BlockSchematicClaimStand extends BlockSchematicClaim {
	public static final PropertyBool CEILING = PropertyBool.create("ceiling");

	public BlockSchematicClaimStand() {
		setRegistryName(Reference.MOD_ID, "schem_block_stand");
		setUnlocalizedName("schem_block_stand");
		setDefaultState(blockState.getBaseState().withProperty(BlockHorizontal.FACING, EnumFacing.NORTH)
				.withProperty(BlockSchematicClaimStand.CEILING, false));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this,
				new IProperty[] { BlockHorizontal.FACING, BlockSchematicClaimStand.CEILING });
	}

	/**
	 * Convert the BlockStateContainer into the correct metadata value
	 */
	@Override
	public int getMetaFromState(IBlockState state) {
		int meta = state.getValue(BlockHorizontal.FACING).getHorizontalIndex();
		if (state.getValue(BlockSchematicClaimStand.CEILING).booleanValue()) {
			meta |= 8;
		}
		return meta;
	}

	/**
	 * Convert the given metadata into a BlockStateContainer for this Block
	 */
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.getHorizontal(meta & 7))
				.withProperty(BlockSchematicClaimStand.CEILING, Boolean.valueOf((meta & 8) > 0));
	}
}