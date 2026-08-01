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
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.PacketDistributor;
import tschipp.carryon.CarryOnCommonClient;
import tschipp.carryon.CarryOnForge;
import tschipp.carryon.Constants;
import tschipp.carryon.carry.CarryOnDataCapability;
import tschipp.carryon.carry.CarryOnDataCapabilityProvider;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.config.BuiltConfig;
import tschipp.carryon.config.forge.ConfigLoaderImpl;
import tschipp.carryon.networking.ClientboundSyncCarryDataPacket;
import tschipp.carryon.networking.PacketBase;
import tschipp.carryon.platform.services.IPlatformHelper;

import java.util.function.BiConsumer;

public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {

        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.isProduction();
    }

    @Override
    public void registerConfig(BuiltConfig cfg) {
        ConfigLoaderImpl.registerConfig(cfg);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends PacketBase, B extends FriendlyByteBuf> void  registerServerboundPacket(CustomPacketPayload.Type<T> type, Class<T> clazz, StreamCodec<B, T> codec, BiConsumer<T, Player> handler, Object... args)
    {
        BiConsumer<T, CustomPayloadEvent.Context> serverHandler = (packet, ctx) -> {
            if(ctx.isServerSide())
            {
                ctx.setPacketHandled(true);
                ctx.enqueueWork(() -> {
                   handler.accept(packet, ctx.getSender());
                });
            }
        };

        CarryOnForge.network.messageBuilder(clazz).codec((StreamCodec<FriendlyByteBuf, T>) codec).consumerMainThread(serverHandler).add();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends PacketBase, B extends FriendlyByteBuf> void  registerClientboundPacket(CustomPacketPayload.Type<T> type, Class<T> clazz, StreamCodec<B, T> codec, BiConsumer<T, Player> handler, Object... args)
    {
        BiConsumer<T, CustomPayloadEvent.Context> clientHandler = (packet, ctx) -> {
            if(ctx.isClientSide())
            {
                ctx.setPacketHandled(true);
                ctx.enqueueWork(() -> {
                    handler.accept(packet, CarryOnCommonClient.getPlayer());
                });
            }
        };

        CarryOnForge.network.messageBuilder(clazz).codec((StreamCodec<FriendlyByteBuf, T>)codec).consumerMainThread(clientHandler).add();
    }


    @Override
    public void sendPacketToServer(Identifier id, PacketBase packet)
    {
        CarryOnForge.network.send(packet, PacketDistributor.SERVER.noArg());
    }

    @Override
    public void sendPacketToPlayer(Identifier id, PacketBase packet, ServerPlayer player)
    {
        CarryOnForge.network.send(packet, PacketDistributor.PLAYER.with(player));
    }

    @Override
    public CarryOnData getCarryData(Player player) {
        var cap = player.getCapability(CarryOnDataCapabilityProvider.CARRY_ON_DATA_CAPABILITY).orElse(new CarryOnDataCapability());
        return cap.getCarryData();
    }

    @Override
    public void setCarryData(Player player, CarryOnData data) {
        var cap = player.getCapability(CarryOnDataCapabilityProvider.CARRY_ON_DATA_CAPABILITY).orElse(new CarryOnDataCapability());
        cap.setCarryData(data);
        if(!player.level().isClientSide()) {
            sendPacketToAllPlayers(Constants.PACKET_ID_SYNC_CARRY_ON_DATA, new ClientboundSyncCarryDataPacket(player.getId(), data), (ServerLevel) player.level());
        }
    }
}
