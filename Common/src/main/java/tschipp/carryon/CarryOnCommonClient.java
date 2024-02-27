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

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import tschipp.carryon.client.keybinds.CarryOnKeybinds;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;

public class CarryOnCommonClient
{
	public static void checkForKeybinds()
	{
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		if(player != null) {
			CarryOnData carry = CarryOnDataManager.getCarryData(player);
			if ((CarryOnKeybinds.carryKey.isUnbound() ? player.isShiftKeyDown() : (CarryOnKeybinds.carryKey.isDown() || checkMouse())) && !carry.isKeyPressed()) {
				CarryOnKeybinds.onCarryKey(true);
				carry.setKeyPressed(true);
				CarryOnDataManager.setCarryData(player, carry);
			} else if (!(CarryOnKeybinds.carryKey.isUnbound() ? player.isShiftKeyDown() : (CarryOnKeybinds.carryKey.isDown() || checkMouse()) ) && carry.isKeyPressed()) {
				CarryOnKeybinds.onCarryKey(false);
				carry.setKeyPressed(false);
				CarryOnDataManager.setCarryData(player, carry);
			}
		}
	}

	private static boolean checkMouse()
	{
		Minecraft mc = Minecraft.getInstance();
		return (CarryOnKeybinds.carryKey.matchesMouse(0) && mc.mouseHandler.isLeftPressed()) || (CarryOnKeybinds.carryKey.matchesMouse(1) && mc.mouseHandler.isRightPressed()) || (CarryOnKeybinds.carryKey.matchesMouse(3) && mc.mouseHandler.isMiddlePressed());
	}

	public static void onCarryClientTick()
	{
		Player player = Minecraft.getInstance().player;
		if(player != null) {
			CarryOnData carry = CarryOnDataManager.getCarryData(player);
			if(carry.isCarrying())
			{
				player.getInventory().selected = carry.getSelected();
			}
		}
	}

	public static Player getPlayer()
	{
		return Minecraft.getInstance().player;
	}
}
