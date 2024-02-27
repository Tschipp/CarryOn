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

package tschipp.carryon.client.keybinds;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import tschipp.carryon.Constants;
import tschipp.carryon.networking.serverbound.ServerboundCarryKeyPressedPacket;
import tschipp.carryon.platform.Services;

import java.util.function.Consumer;

public class CarryOnKeybinds
{
	public static KeyMapping carryKey;

	public static void registerKeybinds(Consumer<KeyMapping> registrar)
	{
		if(Services.PLATFORM.isModLoaded("amecsapi"))
			carryKey = new ConflictFreeKeyMapping("key.carry.desc", InputConstants.KEY_LSHIFT, "key.carry.category");
		else
			carryKey = new ConflictFreeKeyMapping("key.carry.desc", Services.PLATFORM.getPlatformName().equals("Forge") ? InputConstants.KEY_LSHIFT : InputConstants.UNKNOWN.getValue(), "key.carry.category");

		registrar.accept(carryKey);
	}

	public static void onCarryKey(boolean pressed)
	{
		Services.PLATFORM.sendPacketToServer(Constants.PACKET_ID_KEY_PRESSED, new ServerboundCarryKeyPressedPacket(pressed));
	}

}
