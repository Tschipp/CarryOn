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

package tschipp.carryon.platform;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import tschipp.carryon.CarryOnFabricClientMod;
import tschipp.carryon.config.BuiltConfig;
import tschipp.carryon.config.fabric.ConfigLoaderImpl;
import tschipp.carryon.networking.PacketBase;
import tschipp.carryon.platform.services.IPlatformHelper;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public void registerConfig(BuiltConfig cfg) {
        ConfigLoaderImpl.registerConfig(cfg);
    }

    @Override
    public <T extends PacketBase> void registerServerboundPacket(ResourceLocation id, int numericalId, Class<T> clazz, BiConsumer<T, FriendlyByteBuf> writer, Function<FriendlyByteBuf, T> reader, BiConsumer<T, Player> handler, Object... args)
    {
        ServerPlayNetworking.registerGlobalReceiver(id, (server, player, packetHandler, buf, responseSender) -> {
            T packet = reader.apply(buf);
            server.execute(() -> {
                handler.accept(packet, player);
            });
        });
    }

    @Override
    public <T extends PacketBase> void registerClientboundPacket(ResourceLocation id, int numericalId, Class<T> clazz, BiConsumer<T, FriendlyByteBuf> writer, Function<FriendlyByteBuf, T> reader, BiConsumer<T, Player> handler, Object... args)
    {
        CarryOnFabricClientMod.registerClientboundPacket(id, reader, handler);
    }

    @Override
    public void sendPacketToServer(ResourceLocation id, PacketBase packet)
    {
        CarryOnFabricClientMod.sendPacketToServer(id, packet);
    }

    @Override
    public void sendPacketToPlayer(ResourceLocation id, PacketBase packet, ServerPlayer player)
    {
        FriendlyByteBuf buf = PacketByteBufs.create();
        packet.write(buf);
        ServerPlayNetworking.send(player, id, buf);
    }
}
