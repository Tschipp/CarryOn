package tschipp.carryon.networking.serverbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import tschipp.carryon.Constants;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;
import tschipp.carryon.networking.PacketBase;

public record ServerboundCarryKeyPressedPacket(boolean pressed) implements PacketBase
{
	public ServerboundCarryKeyPressedPacket(FriendlyByteBuf buf)
	{
		this(buf.readBoolean());
	}

	@Override
	public void write(FriendlyByteBuf buf)
	{
		buf.writeBoolean(pressed);
	}

	@Override
	public void handle(Player player)
	{
		CarryOnData carry = CarryOnDataManager.getCarryData(player);
		carry.setKeyPressed(this.pressed);
		CarryOnDataManager.setCarryData(player, carry);
	}

	@Override
	public ResourceLocation id() {
		return Constants.PACKET_ID_KEY_PRESSED;
	}
}
