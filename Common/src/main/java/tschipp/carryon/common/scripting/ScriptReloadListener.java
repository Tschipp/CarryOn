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

package tschipp.carryon.common.scripting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import tschipp.carryon.Constants;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.networking.clientbound.ClientboundSyncScriptsPacket;
import tschipp.carryon.platform.Services;

import java.util.Collections;
import java.util.Map;

public class ScriptReloadListener extends SimpleJsonResourceReloadListener
{
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	public ScriptReloadListener()
	{
		super(GSON, "carryon/scripts");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager manager, ProfilerFiller profiler)
	{
		ScriptManager.SCRIPTS.clear();

		try {
			objects.forEach((path, jsonElem) -> {
				DataResult<CarryOnScript> res = CarryOnScript.CODEC.parse(JsonOps.INSTANCE, jsonElem);
				if(res.result().isPresent())
				{
					CarryOnScript script = res.result().get();
					if (script.isValid())
						ScriptManager.SCRIPTS.add(script);
				}
				else
					Constants.LOG.warn("Error while parsing script: " + res.error().get().message());
			});
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		Collections.sort(ScriptManager.SCRIPTS, (s1, s2) -> Long.compare(s2.priority(), s1.priority()));
	}

	public static void syncScriptsWithClient(ServerPlayer player)
	{
		if (player != null)
		{
			DataResult<Tag> result = Codec.list(CarryOnScript.CODEC).encodeStart(NbtOps.INSTANCE, ScriptManager.SCRIPTS);
			Tag tag = result.getOrThrow(false, s -> {throw new RuntimeException("Error while synching Carry On Scripts: " + s);});

			Services.PLATFORM.sendPacketToPlayer(Constants.PACKET_ID_SYNC_SCRIPTS, new ClientboundSyncScriptsPacket(tag), player);
		}
	}
}

