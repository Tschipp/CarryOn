package tschipp.carryon.networking.serverbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;
import tschipp.carryon.networking.PacketBase;

public class ServerboundCarryKeyPressedPacket extends PacketBase
{
	boolean pressed;

	public ServerboundCarryKeyPressedPacket(FriendlyByteBuf buf)
	{
		this.pressed = buf.readBoolean();
	}

	public ServerboundCarryKeyPressedPacket(boolean pressed)
	{
		this.pressed = pressed;
	}

	@Override
	public void toBytes(FriendlyByteBuf buf)
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
}
