package tschipp.carryon.platform;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.PacketDistributor;
import tschipp.carryon.CarryOnCommonClient;
import tschipp.carryon.CarryOnForge;
import tschipp.carryon.config.BuiltConfig;
import tschipp.carryon.config.forge.ConfigLoaderImpl;
import tschipp.carryon.networking.PacketBase;
import tschipp.carryon.platform.services.IPlatformHelper;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
    public <T extends PacketBase> void registerServerboundPacket(ResourceLocation id, int numericalId, Class<T> clazz, BiConsumer<T, FriendlyByteBuf> writer, Function<FriendlyByteBuf, T> reader, BiConsumer<T, Player> handler)
    {
        BiConsumer<T, Supplier<Context>> serverHandler = (packet, ctx) -> {
            if(ctx.get().getDirection().getReceptionSide().isServer())
            {
                ctx.get().setPacketHandled(true);
                ctx.get().enqueueWork(() -> {
                   handler.accept(packet, ctx.get().getSender());
                });
            }
        };

        CarryOnForge.network.registerMessage(numericalId, clazz, writer, reader, serverHandler, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    @Override
    public <T extends PacketBase> void registerClientboundPacket(ResourceLocation id, int numericalId, Class<T> clazz, BiConsumer<T, FriendlyByteBuf> writer, Function<FriendlyByteBuf, T> reader, BiConsumer<T, Player> handler)
    {
        BiConsumer<T, Supplier<Context>> clientHandler = (packet, ctx) -> {
            if(ctx.get().getDirection().getReceptionSide().isClient())
            {
                ctx.get().setPacketHandled(true);
                ctx.get().enqueueWork(() -> {
                    handler.accept(packet, CarryOnCommonClient.getPlayer());
                });
            }
        };

        CarryOnForge.network.registerMessage(numericalId, clazz, writer, reader, clientHandler, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }


    @Override
    public void sendPacketToServer(ResourceLocation id, PacketBase packet)
    {
        CarryOnForge.network.sendToServer(packet);
    }

    @Override
    public void sendPacketToPlayer(ResourceLocation id, PacketBase packet, ServerPlayer player)
    {
        CarryOnForge.network.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
