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

package tschipp.carryon.common.carry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.player.Player;

public class CarryOnDataManager {

    public static final EntityDataAccessor<CompoundTag> CARRY_DATA_KEY = SynchedEntityData.defineId(Player.class, EntityDataSerializers.COMPOUND_TAG);


    public static CarryOnData getCarryData(Player player)
    {
        CompoundTag data = player.getEntityData().get(CARRY_DATA_KEY);
        return new CarryOnData(data.copy());
    }

    public static void setCarryData(Player player, CarryOnData data)
    {
        data.setSelected(player.getInventory().selected);
        CompoundTag nbt = data.getNbt();
        nbt.putInt("tick", player.tickCount);
        player.getEntityData().set(CARRY_DATA_KEY, nbt);
    }

}
