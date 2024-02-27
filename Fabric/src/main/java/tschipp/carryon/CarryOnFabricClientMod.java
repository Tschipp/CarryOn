/*
 * GNU Lesser General Public License v3
 * Copyright (C) 2024 Tschipp
 * mrtschipp@gmail.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

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
