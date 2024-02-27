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

package tschipp.carryon.compat;

import net.minecraft.world.entity.player.Player;
import tschipp.carryon.Constants;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class GamestageCompat
{
	private static Method hasStage;

	static {
		try {
			Class<?> gamestageHelper = Class.forName("net.darkhax.gamestages.GameStageHelper");
			hasStage = gamestageHelper.getMethod("hasStage", Player.class, String.class);

		} catch (Exception e) {
			Constants.LOG.info("Gamestages not found. Disabling.");
		}
	}

	public static boolean hasStage(Player player, String stage)
	{
		if(hasStage == null)
			return true;
		try {
			return (boolean) hasStage.invoke(null, player, stage);
		} catch (IllegalAccessException | InvocationTargetException e) {
		}
		return true;
	}
}
