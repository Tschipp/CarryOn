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

package tschipp.carryon.networking.clientbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import tschipp.carryon.Constants;
import tschipp.carryon.networking.PacketBase;

public record ClientboundStartRidingPacket(int iden, boolean ride) implements PacketBase
{
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundStartRidingPacket> CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, ClientboundStartRidingPacket::iden,
			ByteBufCodecs.BOOL, ClientboundStartRidingPacket::ride,
			ClientboundStartRidingPacket::new
	);

	public static final CustomPacketPayload.Type<ClientboundStartRidingPacket> TYPE = new Type<>(Constants.PACKET_ID_START_RIDING);

	@Override
	public void handle(Player player)
	{
		Entity otherPlayer = player.level().getEntity(this.iden);
		if(otherPlayer != null)
			if(ride)
				otherPlayer.startRiding(player, true);
			else
				otherPlayer.stopRiding();
	}

	@Override
	public Type<ClientboundStartRidingPacket> type() {
		return TYPE;
	}
}
