package com.dyn.schematics.registry;

import java.util.HashSet;
import java.util.Set;
import java.util.function.ToIntFunction;

import com.dyn.schematics.SchematicMod;
import com.dyn.schematics.block.BlockSchematicClaim;
import com.dyn.schematics.block.BlockSchematicClaimStand;
import com.dyn.schematics.reference.Reference;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = Reference.MOD_ID)
public class ModelManager {
	public static final ModelManager INSTANCE = new ModelManager();

	/**
	 * Register this mod's {@link Fluid}, {@link Block} and {@link Item} models.
	 *
	 * @param event
	 *            The event
	 */
	@SubscribeEvent
	public static void registerAllModels(final ModelRegistryEvent event) {
		ModelManager.INSTANCE.registerBlockModels();
		ModelManager.INSTANCE.registerItemModels();
	}

	/**
	 * A {@link StateMapperBase} used to create property strings.
	 */
	private final StateMapperBase propertyStringMapper = new StateMapperBase() {
		@Override
		protected ModelResourceLocation getModelResourceLocation(final IBlockState state) {
			return new ModelResourceLocation("minecraft:air");
		}
	};

	/**
	 * The {@link Item}s that have had models registered so far.
	 */
	private final Set<Item> itemsRegistered = new HashSet<>();

	private ModelManager() {
	}

	/**
	 * Register a model for a metadata value of the {@link Block}'s {@link Item}.
	 * <p>
	 * Uses the registry name as the domain/path and the {@link IBlockState} as the
	 * variant.
	 *
	 * @param state
	 *            The state to use as the variant
	 * @param metadata
	 *            The item metadata to register the model for
	 */
	private void registerBlockItemModel(final IBlockState state) {
		final Block block = state.getBlock();
		final Item item = Item.getItemFromBlock(block);

		if (item != Items.AIR) {
			registerItemModel(item, new ModelResourceLocation(block.getRegistryName(),
					propertyStringMapper.getPropertyString(state.getProperties())));
		}

	}

	/**
	 * Register a model for a metadata value of the {@link Block}'s {@link Item}.
	 * <p>
	 * Uses the registry name as the domain/path and the {@link IBlockState} as the
	 * variant.
	 *
	 * @param state
	 *            The state to use as the variant
	 * @param metadata
	 *            The item metadata to register the model for
	 */
	private void registerBlockItemModelForMeta(final IBlockState state, final int metadata) {
		final Block block = state.getBlock();
		final Item item = Item.getItemFromBlock(block);

		if (item != Items.AIR) {
			registerItemModel(item, new ModelResourceLocation(block.getRegistryName(),
					propertyStringMapper.getPropertyString(state.getProperties())));
		}

	}

	/**
	 * Register this mod's {@link Block} models.
	 */
	private void registerBlockModels() {
		registerVariantBlockItemModels(
				SchematicMod.schematicBlockStand.getDefaultState().withProperty(BlockSchematicClaimStand.CEILING, true),
				BlockSchematicClaim.FACING, EnumFacing::getIndex);
		registerVariantBlockItemModels(SchematicMod.schematicBlockStand.getDefaultState().withProperty(
				BlockSchematicClaimStand.CEILING, false), BlockSchematicClaim.FACING, EnumFacing::getIndex);
		registerVariantBlockItemModels(SchematicMod.schematicBlockWall.getDefaultState(), BlockSchematicClaim.FACING,
				EnumFacing::getIndex);
		// registerItemModel(SchematicMod.desk.getItemBlock(), Reference.MOD_ID +
		// ":architect_desk");
		registerBlockItemModel(SchematicMod.desk.getDefaultState());
	}

	/**
	 * Register an {@link ItemMeshDefinition} for an {@link Item}.
	 *
	 * @param item
	 *            The Item
	 * @param meshDefinition
	 *            The ItemMeshDefinition
	 */
	private void registerItemModel(final Item item, final ItemMeshDefinition meshDefinition) {
		itemsRegistered.add(item);
		ModelLoader.setCustomMeshDefinition(item, meshDefinition);
	}

	/**
	 * Register a single model for an {@link Item}.
	 * <p>
	 * Uses {@code fullModelLocation} as the domain, path and variant.
	 *
	 * @param item
	 *            The Item
	 * @param fullModelLocation
	 *            The full model location
	 */
	private void registerItemModel(final Item item, final ModelResourceLocation fullModelLocation) {
		ModelBakery.registerItemVariants(item, fullModelLocation);
		// Ensure the custom model is loaded and prevent the
		// default model from being loaded
		registerItemModel(item, stack -> fullModelLocation);
	}

	/**
	 * Register this mod's {@link Item} models.
	 */
	private void registerItemModels() {
		registerRenderVariants(SchematicMod.schematic, "schematic", 2);
		// registerItemModel(SchematicMod.schematic, Reference.MOD_ID + ":schematic");
	}

	public void registerRenderVariants(Item item, String name, int amount) {
		ResourceLocation[] variants = new ResourceLocation[amount];
		for (int i = 0; i < amount; i++) {
			variants[i] = new ResourceLocation(Reference.MOD_ID, name + "_" + i);
			ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(variants[i], "inventory"));
		}
		ModelBakery.registerItemVariants(item, variants);
	}

	/**
	 * Register a model for each metadata value of the {@link Block}'s {@link Item}
	 * corresponding to the values of an {@link IProperty}.
	 * <p>
	 * For each value:
	 * <li>The domain/path is the registry name</li>
	 * <li>The variant is {@code baseState} with the {@link IProperty} set to the
	 * value</li>
	 * <p>
	 * The {@code getMeta} function is used to get the metadata of each value.
	 *
	 * @param baseState
	 *            The base state to use for the variant
	 * @param property
	 *            The property whose values should be used
	 * @param getMeta
	 *            A function to get the metadata of each value
	 * @param <T>
	 *            The value type
	 */
	private <T extends Comparable<T>> void registerVariantBlockItemModels(final IBlockState baseState,
			final IProperty<T> property, final ToIntFunction<T> getMeta) {
		property.getAllowedValues()
				.forEach(value -> registerBlockItemModelForMeta(baseState.withProperty(property, value),
						getMeta.applyAsInt(value)));
	}
}