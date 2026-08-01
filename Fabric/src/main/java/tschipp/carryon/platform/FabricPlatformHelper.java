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

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import tschipp.carryon.CarryOnFabricClientMod;
import tschipp.carryon.CarryOnFabricMod;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.config.BuiltConfig;
import tschipp.carryon.config.fabric.ConfigLoaderImpl;
import tschipp.carryon.networking.PacketBase;
import tschipp.carryon.platform.services.IPlatformHelper;

import java.util.function.BiConsumer;

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

    @SuppressWarnings("unchecked")
    @Override
    public <T extends PacketBase, B extends FriendlyByteBuf> void  registerServerboundPacket(CustomPacketPayload.Type<T> type, Class<T> clazz, StreamCodec<B, T> codec, BiConsumer<T, Player> handler, Object... args)
    {
        PayloadTypeRegistry.serverboundPlay().register(type, (StreamCodec<RegistryFriendlyByteBuf, T>)codec);

        ServerPlayNetworking.registerGlobalReceiver(type, (T packet, ServerPlayNetworking.Context context) -> {
            context.server().execute(() -> {
                handler.accept(packet, context.player());
            });
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends PacketBase, B extends FriendlyByteBuf> void  registerClientboundPacket(CustomPacketPayload.Type<T> type, Class<T> clazz, StreamCodec<B, T> codec, BiConsumer<T, Player> handler, Object... args)
    {
        boolean client = (boolean)args[0];

        if(!client)
            PayloadTypeRegistry.clientboundPlay().register(type, (StreamCodec<RegistryFriendlyByteBuf, T>)codec);
        else
            CarryOnFabricClientMod.registerClientboundPacket(type, handler);
    }

    @Override
    public void sendPacketToServer(Identifier id, PacketBase packet)
    {
        CarryOnFabricClientMod.sendPacketToServer(packet);
    }

    @Override
    public void sendPacketToPlayer(Identifier id, PacketBase packet, ServerPlayer player)
    {
        ServerPlayNetworking.send(player, packet);
    }

    @Override
    public CarryOnData getCarryData(Player player) {
        CarryOnData data = player.getAttachedOrCreate(CarryOnFabricMod.CARRY_ON_DATA_ATTACHMENT_TYPE);
        return data.clone();
    }

    @Override
    public void setCarryData(Player player, CarryOnData data) {
        player.setAttached(CarryOnFabricMod.CARRY_ON_DATA_ATTACHMENT_TYPE, data);
    }
}
