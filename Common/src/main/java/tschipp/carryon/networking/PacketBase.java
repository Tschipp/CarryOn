package tschipp.carryon.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public interface PacketBase
{
	void write(FriendlyByteBuf buf);

	void handle(Player player);

	ResourceLocation id();
}
