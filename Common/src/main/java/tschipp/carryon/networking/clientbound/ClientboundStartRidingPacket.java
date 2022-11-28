package tschipp.carryon.networking.clientbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import tschipp.carryon.networking.PacketBase;

public class ClientboundStartRidingPacket extends PacketBase
{
	int entityId;
	boolean ride;

	public ClientboundStartRidingPacket(FriendlyByteBuf buf)
	{
		this.entityId = buf.readInt();
		this.ride = buf.readBoolean();
	}

	public ClientboundStartRidingPacket(int id, boolean ride)
	{
		this.entityId = id;
		this.ride = ride;
	}

	@Override
	public void toBytes(FriendlyByteBuf buf)
	{
		buf.writeInt(entityId);
		buf.writeBoolean(ride);
	}

	@Override
	public void handle(Player player)
	{
		Entity otherPlayer = player.level.getEntity(this.entityId);
		if(otherPlayer != null)
			if(ride)
				otherPlayer.startRiding(player);
			else
				otherPlayer.stopRiding();
	}
}
