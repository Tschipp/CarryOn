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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import tschipp.carryon.Constants;
import tschipp.carryon.networking.PacketBase;

public record ClientboundStartRidingPacket(int iden, boolean ride) implements PacketBase
{
	public ClientboundStartRidingPacket(FriendlyByteBuf buf)
	{
		this(buf.readInt(), buf.readBoolean());
	}

	@Override
	public void write(FriendlyByteBuf buf)
	{
		buf.writeInt(iden);
		buf.writeBoolean(ride);
	}

	@Override
	public void handle(Player player)
	{
		Entity otherPlayer = player.level().getEntity(this.iden);
		if(otherPlayer != null)
			if(ride)
				otherPlayer.startRiding(player);
			else
				otherPlayer.stopRiding();
	}

	@Override
	public ResourceLocation id() {
		return Constants.PACKET_ID_START_RIDING;
	}
}
