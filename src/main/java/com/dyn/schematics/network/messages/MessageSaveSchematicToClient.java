package com.dyn.schematics.network.messages;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.dyn.schematics.SchematicMod;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageSaveSchematicToClient implements IMessage {

	public static class Handler implements IMessageHandler<MessageSaveSchematicToClient, IMessage> {
		@Override
		public IMessage onMessage(final MessageSaveSchematicToClient message, final MessageContext ctx) {
			SchematicMod.proxy.addScheduledTask(() -> {
				try {
					DataOutputStream dataoutputstream = new DataOutputStream(new FileOutputStream(
							new File(Minecraft.getMinecraft().mcDataDir, "schematics/" + message.getName())));
					CompressedStreamTools.writeCompressed(message.getTag(), dataoutputstream);
				} catch (IOException e) {
					SchematicMod.logger.error("Encountered error trying to save schematic", e);
				}
			});
			return null;
		}
	}

	private NBTTagCompound tag;

	private String name;

	public MessageSaveSchematicToClient() {
	}

	public MessageSaveSchematicToClient(String name, NBTTagCompound tag) {
		this.name = name;
		this.tag = tag;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		tag = ByteBufUtils.readTag(buf);
		name = ByteBufUtils.readUTF8String(buf);
	}

	public String getName() {
		return name;
	}

	public NBTTagCompound getTag() {
		return tag;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeTag(buf, tag);
		ByteBufUtils.writeUTF8String(buf, name);
	}

}
