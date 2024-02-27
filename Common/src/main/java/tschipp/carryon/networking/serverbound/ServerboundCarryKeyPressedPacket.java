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

package tschipp.carryon.networking.serverbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import tschipp.carryon.Constants;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;
import tschipp.carryon.networking.PacketBase;

public record ServerboundCarryKeyPressedPacket(boolean pressed) implements PacketBase
{
	public ServerboundCarryKeyPressedPacket(FriendlyByteBuf buf)
	{
		this(buf.readBoolean());
	}

	@Override
	public void write(FriendlyByteBuf buf)
	{
		buf.writeBoolean(pressed);
	}

	@Override
	public void handle(Player player)
	{
		CarryOnData carry = CarryOnDataManager.getCarryData(player);
		carry.setKeyPressed(this.pressed);
		CarryOnDataManager.setCarryData(player, carry);
	}

	@Override
	public ResourceLocation id() {
		return Constants.PACKET_ID_KEY_PRESSED;
	}
}
