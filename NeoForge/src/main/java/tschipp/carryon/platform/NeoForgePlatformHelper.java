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
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPlayPayloadHandler;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import tschipp.carryon.CarryOnCommonClient;
import tschipp.carryon.config.BuiltConfig;
import tschipp.carryon.config.neoforge.ConfigLoaderImpl;
import tschipp.carryon.networking.PacketBase;
import tschipp.carryon.platform.services.IPlatformHelper;

import java.util.function.BiConsumer;
import java.util.function.Function;

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

    private record PacketBridge<T extends PacketBase>(T packet) implements CustomPacketPayload {

        @Override
        public void write(FriendlyByteBuf pBuffer) {
            packet.write(pBuffer);
        }

        @Override
        public ResourceLocation id() {
            return packet.id();
        }

        public T original() {
            return packet;
        }
    }

    @Override
    public <T extends PacketBase> void registerServerboundPacket(ResourceLocation id, int numericalId, Class<T> clazz, BiConsumer<T, FriendlyByteBuf> writer, Function<FriendlyByteBuf, T> reader, BiConsumer<T, Player> handler, Object... args) {
        IPayloadRegistrar registrar = (IPayloadRegistrar) args[0];

        IPlayPayloadHandler<PacketBridge<T>> serverHandler = (packet, ctx) -> {
            ctx.workHandler().submitAsync(() -> {
                handler.accept(packet.original(), ctx.player().get());
            });
        };

        FriendlyByteBuf.Reader<PacketBridge<T>> modifiedReader = (buf) -> new PacketBridge<T>(reader.apply(buf));

        registrar.play(id, modifiedReader, han -> han.server(serverHandler));
    }

    @Override
    public <T extends PacketBase> void registerClientboundPacket(ResourceLocation id, int numericalId, Class<T> clazz, BiConsumer<T, FriendlyByteBuf> writer, Function<FriendlyByteBuf, T> reader, BiConsumer<T, Player> handler, Object... args) {
        IPayloadRegistrar registrar = (IPayloadRegistrar) args[0];

        IPlayPayloadHandler<PacketBridge<T>> clientHandler = (packet, ctx) -> {
            ctx.workHandler().submitAsync(() -> {
                handler.accept(packet.original(), CarryOnCommonClient.getPlayer());
            });
        };

        FriendlyByteBuf.Reader<PacketBridge<T>> modifiedReader = (buf) -> new PacketBridge<T>(reader.apply(buf));

        registrar.play(id, modifiedReader, han -> han.client(clientHandler));
    }


    @Override
    public void sendPacketToServer(ResourceLocation id, PacketBase packet) {
        PacketDistributor.SERVER.noArg().send(new PacketBridge(packet));
    }

    @Override
    public void sendPacketToPlayer(ResourceLocation id, PacketBase packet, ServerPlayer player) {
        PacketDistributor.PLAYER.with(player).send(new PacketBridge(packet));
    }
}
