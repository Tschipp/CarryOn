package tschipp.carryon.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public abstract class PacketBase
{
	public abstract void toBytes(FriendlyByteBuf buf);

	public abstract void handle(Player player);
}
