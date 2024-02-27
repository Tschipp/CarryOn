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

package tschipp.carryon.platform.services;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import tschipp.carryon.config.BuiltConfig;
import tschipp.carryon.networking.PacketBase;

import java.util.function.BiConsumer;
import java.util.function.Function;

public interface IPlatformHelper {

    /**
     * Gets the name of the current platform
     *
     * @return The name of the current platform.
     */
    String getPlatformName();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    void registerConfig(BuiltConfig cfg);

    <T extends PacketBase> void  registerServerboundPacket(ResourceLocation id, int numericalId, Class<T> clazz, BiConsumer<T, FriendlyByteBuf> writer, Function<FriendlyByteBuf, T> reader, BiConsumer<T, Player> handler, Object... args);

    <T extends PacketBase> void  registerClientboundPacket(ResourceLocation id, int numericalId, Class<T> clazz, BiConsumer<T, FriendlyByteBuf> writer, Function<FriendlyByteBuf, T> reader, BiConsumer<T, Player> handler, Object... args);

    void sendPacketToServer(ResourceLocation id, PacketBase packet);

    void sendPacketToPlayer(ResourceLocation id, PacketBase packet, ServerPlayer player);

}
