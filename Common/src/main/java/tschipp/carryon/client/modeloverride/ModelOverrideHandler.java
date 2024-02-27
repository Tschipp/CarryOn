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

package tschipp.carryon.client.modeloverride;

import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import tschipp.carryon.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ModelOverrideHandler
{
	private static List<ModelOverride> OVERRIDES = new ArrayList<>();

	public static void initModelOverrides()
	{
		OVERRIDES.clear();

		for(String ov : Constants.CLIENT_CONFIG.modelOverrides)
		{
			addFromString(ov);
		}
	}

	public static Optional<ModelOverride> getModelOverride(BlockState state, @Nullable CompoundTag tag)
	{
		for(ModelOverride ov : OVERRIDES)
		{
			if(ov.matches(state, tag))
				return Optional.of(ov);
		}
		return Optional.empty();
	}

	public static void addFromString(String str)
	{
		DataResult<ModelOverride> res = ModelOverride.of(str);
		if(res.result().isPresent())
		{
			ModelOverride override = res.result().get();
			OVERRIDES.add(override);
		}
		else
		{
			Constants.LOG.debug("Error while parsing ModelOverride: " + res.error().get().message());
		}
	}

}
