package com.dyn.schematics.proxy;

import java.io.File;

import org.lwjgl.input.Keyboard;

import com.dyn.schematics.SchematicMod;
import com.dyn.schematics.registry.SchematicRegistry;
import com.dyn.schematics.registry.SchematicRenderingRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class Client implements Proxy {

	private KeyBinding schemKey;

	@Override
	public void addScheduledTask(Runnable runnable) {
		Minecraft.getMinecraft().addScheduledTask(runnable);
	}

	@Override
	public EntityPlayer getPlayerEntity(MessageContext ctx) {
		// Note that if you simply return 'Minecraft.getMinecraft().thePlayer',
		// your packets will not work as expected because you will be getting a
		// client player even when you are on the server!
		// Sounds absurd, but it's true.

		// Solution is to double-check side before returning the player:
		return ctx.side.isClient() ? Minecraft.getMinecraft().player : ctx.getServerHandler().player;
	}

	@Override
	public IThreadListener getThreadFromContext(MessageContext ctx) {
		// this causes null pointers in single player...
		return Minecraft.getMinecraft();
	}

	@Override
	public void init() {
		MinecraftForge.EVENT_BUS.register(new SchematicRenderingRegistry());
		MinecraftForge.EVENT_BUS.register(this);
		schemKey = new KeyBinding("key.toggle.schemui", Keyboard.KEY_U, "key.categories.toggle");

		ClientRegistry.registerKeyBinding(schemKey);
	}

	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		if ((Minecraft.getMinecraft().currentScreen instanceof GuiChat)) {
			return;
		}

		if (schemKey.isPressed()) {
			if (!Minecraft.getMinecraft().player.getHeldItemMainhand().hasTagCompound()
					&& !SchematicMod.startPos.equals(BlockPos.ORIGIN) && !SchematicMod.endPos.equals(BlockPos.ORIGIN)) {
				openSchematicGui();
			}
		}
	}

	@Override
	public void openSchematicGui() {
		Minecraft.getMinecraft().displayGuiScreen(new GuiYesNo((result, id) -> {
			if (result) {
				Minecraft.getMinecraft().player.sendChatMessage(String
						.format("/saveschematic " + SchematicMod.startPos.getX() + " " + SchematicMod.startPos.getY()
								+ " " + SchematicMod.startPos.getZ() + " " + SchematicMod.endPos.getX() + " "
								+ SchematicMod.endPos.getY() + " " + SchematicMod.endPos.getZ()));
			}
			SchematicMod.startPos = BlockPos.ORIGIN;
			SchematicMod.endPos = BlockPos.ORIGIN;
			Minecraft.getMinecraft().displayGuiScreen(null);
		}, "Save Schematic", "Would you like to save this schematic?", 1));
	}

	@Override
	public void postInit() {
		// TODO Auto-generated method stub

	}

	@Override
	public void preInit() {
		File schematicLocs = new File(Minecraft.getMinecraft().mcDataDir, "schematics");

		if (!schematicLocs.exists()) {
			schematicLocs.mkdir();
		}

		SchematicRegistry.addSchematicLocation(schematicLocs);
	}
}