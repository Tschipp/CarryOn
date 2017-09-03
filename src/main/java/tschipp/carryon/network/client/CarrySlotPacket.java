package tschipp.carryon.network.client;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class CarrySlotPacket implements IMessage
{
	public int slot;
	
	public CarrySlotPacket()
	{
	}
	
	public CarrySlotPacket(int slot)
	{
		this.slot = slot;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.slot = ByteBufUtils.readVarInt(buf, 4);
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		ByteBufUtils.writeVarInt(buf, slot, 4);
	}

}
