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
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import tschipp.carryon.CarryOnCommonClient;
import tschipp.carryon.CarryOnNeoForge;
import tschipp.carryon.CarryOnNeoForgeClient;
import tschipp.carryon.Constants;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.config.BuiltConfig;
import tschipp.carryon.config.neoforge.ConfigLoaderImpl;
import tschipp.carryon.networking.PacketBase;
import tschipp.carryon.networking.clientbound.ClientboundSyncCarryDataPacket;
import tschipp.carryon.platform.services.IPlatformHelper;

import java.util.function.BiConsumer;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {

        return "NeoForge";
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
    public <T extends PacketBase, B extends FriendlyByteBuf> void registerServerboundPacket(CustomPacketPayload.Type<T> type, Class<T> clazz, StreamCodec<B, T> codec, BiConsumer<T, Player> handler, Object... args) {
        PayloadRegistrar registrar = (PayloadRegistrar) args[0];

        IPayloadHandler<T> serverHandler = (packet, ctx) -> {
            ctx.enqueueWork(() -> {
                handler.accept(packet, ctx.player());
            });
        };

        registrar.playToServer(type, (StreamCodec<RegistryFriendlyByteBuf, T>)codec, serverHandler);
    }

    @Override
    public <T extends PacketBase, B extends FriendlyByteBuf> void registerClientboundPacket(CustomPacketPayload.Type<T> type, Class<T> clazz, StreamCodec<B, T> codec, BiConsumer<T, Player> handler, Object... args)
    {
        PayloadRegistrar registrar = (PayloadRegistrar) args[0];

        IPayloadHandler<T> clientHandler = (packet, ctx) -> {
            ctx.enqueueWork(() -> {
                handler.accept(packet, CarryOnCommonClient.getPlayer());
            });
        };

        registrar.playToClient(type, (StreamCodec<RegistryFriendlyByteBuf, T>)codec, clientHandler);
    }


    @Override
    public void sendPacketToServer(ResourceLocation id, PacketBase packet) {
        CarryOnNeoForgeClient.sendPacketToServer(packet);
    }

    @Override
    public void sendPacketToPlayer(ResourceLocation id, PacketBase packet, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    @Override
    public CarryOnData getCarryData(Player player) {
        return player.getData(CarryOnNeoForge.CARRY_ON_DATA_ATTACHMENT);
    }

    @Override
    public void setCarryData(Player player, CarryOnData data) {
        player.setData(CarryOnNeoForge.CARRY_ON_DATA_ATTACHMENT, data);
        if(!player.level().isClientSide) {
            sendPacketToAllPlayers(Constants.PACKET_ID_SYNC_CARRY_ON_DATA, new ClientboundSyncCarryDataPacket(player.getId(), data.clone()), (ServerLevel) player.level());
        }
    }
}
