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

import com.mojang.blaze3d.platform.InputConstants.Type;
import net.minecraft.client.KeyMapping;

public class ConflictFreeKeyMapping extends KeyMapping
{
	public ConflictFreeKeyMapping(String $$0, int $$1, String $$2)
	{
		super($$0, $$1, $$2);
	}

	public ConflictFreeKeyMapping(String $$0, Type $$1, int $$2, String $$3)
	{
		super($$0, $$1, $$2, $$3);
	}

	@Override
	public boolean same(KeyMapping $$0)
	{
		return false;
	}
}
