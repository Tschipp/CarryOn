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

package tschipp.carryon.utils;

import tschipp.carryon.Constants;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;


public class SizeHelper
{
	public static float getPlayerScaleFactor(ServerPlayer player)
	{
		if (Constants.COMMON_CONFIG.settings.relativePlayerScale)
			return 1;
		double result = player.getAttributeValue(Attributes.SCALE) / player.getAttributeBaseValue(Attributes.SCALE);
		return (float) result;
	}

	public static float getEntityWidth(Entity entity)
	{
		float result = 1;
		if (entity.tickCount == 0) // catch this specific edge case
		{
			result = entity.getDimensions(entity.getPose()).width();
		}
		else // otherwise this is correct
		{
			result = entity.getBbWidth();
		}
		return result;
	}

	public static float getRelativeEntityWidth(ServerPlayer player, Entity entity)
	{
		return getEntityWidth(entity) * getPlayerScaleFactor(player);
	}

	public static float getEntityHeight(Entity entity)
	{
		float result = 1;
		if (entity.tickCount == 0) // catch this specific edge case
		{
			result = entity.getDimensions(entity.getPose()).width();
		}
		else // otherwise this is correct
		{
			result = entity.getBbWidth();
		}
		return result;
	}

	public static float getRelativeEntityHeight(ServerPlayer player, Entity entity)
	{
		return getEntityHeight(entity) * getPlayerScaleFactor(player);
	}

	public static float getRelativeEntityArea(ServerPlayer player, Entity entity)
	{
		return getEntityHeight(entity) * getEntityWidth(entity) * getPlayerScaleFactor(player);
	}
}
