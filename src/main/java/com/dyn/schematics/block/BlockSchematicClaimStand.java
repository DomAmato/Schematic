package com.dyn.schematics.block;

import com.dyn.schematics.reference.Reference;

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
		setDefaultState(blockState.getBaseState().withProperty(BlockSchematicClaim.FACING, EnumFacing.NORTH)
				.withProperty(BlockSchematicClaimStand.CEILING, false));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this,
				new IProperty[] { BlockSchematicClaim.FACING, BlockSchematicClaimStand.CEILING });
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
		EnumFacing enumfacing = EnumFacing.getFront(meta & 7);

		return getDefaultState().withProperty(BlockSchematicClaim.FACING, enumfacing)
				.withProperty(BlockSchematicClaimStand.CEILING, Boolean.valueOf((meta & 8) > 0));
	}
}