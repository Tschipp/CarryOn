package tschipp.carryon.networking.clientbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import tschipp.carryon.Constants;
import tschipp.carryon.networking.PacketBase;

public record ClientboundStartRidingPacket(int iden, boolean ride) implements PacketBase
{
	public ClientboundStartRidingPacket(FriendlyByteBuf buf)
	{
		this(buf.readInt(), buf.readBoolean());
	}

	@Override
	public void write(FriendlyByteBuf buf)
	{
		buf.writeInt(iden);
		buf.writeBoolean(ride);
	}

	@Override
	public void handle(Player player)
	{
		Entity otherPlayer = player.level().getEntity(this.iden);
		if(otherPlayer != null)
			if(ride)
				otherPlayer.startRiding(player);
			else
				otherPlayer.stopRiding();
	}

	@Override
	public ResourceLocation id() {
		return Constants.PACKET_ID_START_RIDING;
	}
}
