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

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import tschipp.carryon.CarryOnCommonClient;
import tschipp.carryon.CarryOnForge;
import tschipp.carryon.config.BuiltConfig;
import tschipp.carryon.config.forge.ConfigLoaderImpl;
import tschipp.carryon.networking.PacketBase;
import tschipp.carryon.platform.services.IPlatformHelper;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {

        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.isProduction();
    }

    @Override
    public void registerConfig(BuiltConfig cfg) {
        ConfigLoaderImpl.registerConfig(cfg);
    }

    @Override
    public <T extends PacketBase> void registerServerboundPacket(ResourceLocation id, int numericalId, Class<T> clazz, BiConsumer<T, FriendlyByteBuf> writer, Function<FriendlyByteBuf, T> reader, BiConsumer<T, Player> handler, Object... args)
    {
        BiConsumer<T, CustomPayloadEvent.Context> serverHandler = (packet, ctx) -> {
            if(ctx.getDirection().getReceptionSide().isServer())
            {
                ctx.setPacketHandled(true);
                ctx.enqueueWork(() -> {
                   handler.accept(packet, ctx.getSender());
                });
            }
        };

        CarryOnForge.network.messageBuilder(clazz, numericalId, NetworkDirection.PLAY_TO_SERVER)
                .encoder(writer)
                .decoder(reader)
                .consumerMainThread(serverHandler)
                .add();
    }

    @Override
    public <T extends PacketBase> void registerClientboundPacket(ResourceLocation id, int numericalId, Class<T> clazz, BiConsumer<T, FriendlyByteBuf> writer, Function<FriendlyByteBuf, T> reader, BiConsumer<T, Player> handler, Object... args)
    {
        BiConsumer<T, CustomPayloadEvent.Context> clientHandler = (packet, ctx) -> {
            if(ctx.getDirection().getReceptionSide().isClient())
            {
                ctx.setPacketHandled(true);
                ctx.enqueueWork(() -> {
                    handler.accept(packet, CarryOnCommonClient.getPlayer());
                });
            }
        };

        CarryOnForge.network.messageBuilder(clazz, numericalId, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(writer)
                .decoder(reader)
                .consumerMainThread(clientHandler)
                .add();
    }


    @Override
    public void sendPacketToServer(ResourceLocation id, PacketBase packet)
    {
        CarryOnForge.network.send(packet, PacketDistributor.SERVER.noArg());
    }

    @Override
    public void sendPacketToPlayer(ResourceLocation id, PacketBase packet, ServerPlayer player)
    {
        CarryOnForge.network.send(packet, PacketDistributor.PLAYER.with(player));
    }
}
