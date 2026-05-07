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
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import tschipp.carryon.client.keybinds.CarryOnKeybinds;
import tschipp.carryon.events.ClientEvents;
import tschipp.carryon.networking.PacketBase;

import java.util.function.BiConsumer;

public class CarryOnFabricClientMod implements ClientModInitializer
{
	@Override
	public void onInitializeClient()
	{
		CarryOnKeybinds.registerKeybinds(KeyMappingHelper::registerKeyMapping);
		ClientEvents.registerEvents();
		CarryOnCommon.registerClientPackets(true);
	}

	public static void sendPacketToServer(PacketBase packet)
	{
		ClientPlayNetworking.send(packet);
	}

	public static <T extends PacketBase> void registerClientboundPacket(CustomPacketPayload.Type<T> id, BiConsumer<T, Player> handler)
	{
		ClientPlayNetworking.registerGlobalReceiver(id, (T packet, ClientPlayNetworking.Context context) -> {
			context.client().execute(() -> {
				handler.accept(packet, context.player());
			});
		});
	}
}
