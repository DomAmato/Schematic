package com.dyn.schematics.network;

import com.dyn.schematics.network.messages.MessageBuildSchematicFromTileEntity;
import com.dyn.schematics.network.messages.MessageSaveSchematicToClient;
import com.dyn.schematics.network.messages.MessageUpdateSchematicNBT;
import com.dyn.schematics.reference.Reference;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

/**
 *
 * This class will house the SimpleNetworkWrapper instance, which I will name
 * 'dispatcher', as well as give us a logical place from which to register our
 * packets. These two things could be done anywhere, however, even in your Main
 * class, but I will be adding other functionality (see below) that gives this
 * class a bit more utility.
 *
 * While unnecessary, I'm going to turn this class into a 'wrapper' for
 * SimpleNetworkWrapper so that instead of writing
 * "PacketDispatcher.dispatcher.{method}" I can simply write
 * "PacketDispatcher.{method}" All this does is make it quicker to type and
 * slightly shorter; if you do not care about that, then make the 'dispatcher'
 * field public instead of private, or, if you do not want to add a new class
 * just for one field and one static method that you could put anywhere, feel
 * free to put them wherever.
 *
 * For further convenience, I have also added two extra sendToAllAround methods:
 * one which takes an EntityPlayer and one which takes coordinates.
 *
 */
public class NetworkManager {
	// a simple counter will allow us to get rid of 'magic' numbers used during
	// packet registration
	private static byte packetId = 0;

	/**
	 * The SimpleNetworkWrapper instance is used both to register and send packets.
	 * Since I will be adding wrapper methods, this field is private, but you should
	 * make it public if you plan on using it directly.
	 */
	private static SimpleNetworkWrapper dispatcher = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MOD_ID);

	// this is built so that message and handler can be separate classes
	private static <T extends IMessage, U extends IMessageHandler<T, IMessage>> void registerMessage(
			Class<T> message_clazz, Class<U> handler_clazz, Side side) {
		if (side != null) {
			NetworkManager.dispatcher.registerMessage(handler_clazz, message_clazz, NetworkManager.packetId++, side);
		}
	}

	public static void registerMessages() {
		// Server
		NetworkManager.registerMessage(MessageBuildSchematicFromTileEntity.class,
				MessageBuildSchematicFromTileEntity.Handler.class, Side.SERVER);
		NetworkManager.registerMessage(MessageUpdateSchematicNBT.class, MessageUpdateSchematicNBT.Handler.class,
				Side.SERVER);
		NetworkManager.registerMessage(MessageSaveSchematicToClient.class, MessageSaveSchematicToClient.Handler.class,
				Side.CLIENT);

	}

	/**
	 * Send this message to the specified player's client-side counterpart. See
	 * {@link SimpleNetworkWrapper#sendTo(IMessage, EntityPlayerMP)}
	 */
	public static void sendTo(IMessage message, EntityPlayerMP player) {
		NetworkManager.dispatcher.sendTo(message, player);
	}

	/**
	 * Send this message to everyone. See
	 * {@link SimpleNetworkWrapper#sendToAll(IMessage)}
	 */
	public static void sendToAll(IMessage message) {
		NetworkManager.dispatcher.sendToAll(message);
	}

	/**
	 * Sends a message to everyone within a certain range of the player provided.
	 * Shortcut to
	 * {@link SimpleNetworkWrapper#sendToAllAround(IMessage, NetworkRegistry.TargetPoint)}
	 */
	public static void sendToAllAround(IMessage message, EntityPlayer player, double range) {
		NetworkManager.sendToAllAround(message, player.world.provider.getDimension(), player.posX, player.posY,
				player.posZ, range);
	}

	/**
	 * Sends a message to everyone within a certain range of the coordinates in the
	 * same dimension. Shortcut to
	 * {@link SimpleNetworkWrapper#sendToAllAround(IMessage, NetworkRegistry.TargetPoint)}
	 */
	public static void sendToAllAround(IMessage message, int dimension, double x, double y, double z, double range) {
		NetworkManager.sendToAllAround(message, new NetworkRegistry.TargetPoint(dimension, x, y, z, range));
	}

	/**
	 * Send this message to everyone within a certain range of a point. See
	 * {@link SimpleNetworkWrapper#sendToAllAround(IMessage, NetworkRegistry.TargetPoint)}
	 */
	public static void sendToAllAround(IMessage message, NetworkRegistry.TargetPoint point) {
		NetworkManager.dispatcher.sendToAllAround(message, point);
	}

	/**
	 * Send this message to everyone within the supplied dimension. See
	 * {@link SimpleNetworkWrapper#sendToDimension(IMessage, int)}
	 */
	public static void sendToDimension(IMessage message, int dimensionId) {
		NetworkManager.dispatcher.sendToDimension(message, dimensionId);
	}

	/**
	 * Send this message to the server. See
	 * {@link SimpleNetworkWrapper#sendToServer(IMessage)}
	 */
	public static void sendToServer(IMessage message) {
		NetworkManager.dispatcher.sendToServer(message);
	}
}
