package tschipp.carryon;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import tschipp.carryon.client.keybinds.CarryOnKeybinds;
import tschipp.carryon.events.ClientEvents;
import tschipp.carryon.networking.PacketBase;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class CarryOnFabricClientMod implements ClientModInitializer
{
	@Override
	public void onInitializeClient()
	{
		CarryOnKeybinds.registerKeybinds(KeyBindingHelper::registerKeyBinding);
		ClientEvents.registerEvents();
		CarryOnCommon.registerClientPackets();
	}

	public static void sendPacketToServer(ResourceLocation id, PacketBase packet)
	{
		FriendlyByteBuf buf = PacketByteBufs.create();
		packet.write(buf);
		ClientPlayNetworking.send(id, buf);
	}

	public static <T extends PacketBase> void registerClientboundPacket(ResourceLocation id, Function<FriendlyByteBuf, T> reader, BiConsumer<T, Player> handler)
	{
		ClientPlayNetworking.registerGlobalReceiver(id, (client, packetHandler, buf, responseSender) -> {
			T packet = reader.apply(buf);
			client.execute(() -> {
				handler.accept(packet, client.player);
			});
		});
	}
}
