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

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import tschipp.carryon.Constants;
import tschipp.carryon.common.scripting.CarryOnScript;
import tschipp.carryon.common.scripting.ScriptManager;
import tschipp.carryon.networking.PacketBase;

import java.util.List;

public record ClientboundSyncScriptsPacket(Tag serialized) implements PacketBase
{
	public ClientboundSyncScriptsPacket(FriendlyByteBuf buf)
	{
		this(buf.readNbt().get("data"));
	}

	@Override
	public void write(FriendlyByteBuf buf)
	{
		CompoundTag tag = new CompoundTag();
		tag.put("data", serialized);
		buf.writeNbt(tag);
	}

	@Override
	public void handle(Player player)
	{
		DataResult<List<CarryOnScript>> res = Codec.list(CarryOnScript.CODEC).parse(NbtOps.INSTANCE, serialized);
		List<CarryOnScript> scripts = res.getOrThrow(false, (s) -> {throw new RuntimeException("Failed deserializing carry on scripts on the client: " + s);});
		ScriptManager.SCRIPTS.clear();
		ScriptManager.SCRIPTS.addAll(scripts);
	}

	@Override
	public ResourceLocation id() {
		return Constants.PACKET_ID_SYNC_SCRIPTS;
	}
}
