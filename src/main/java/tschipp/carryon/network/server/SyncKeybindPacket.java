package tschipp.carryon.network.server;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class SyncKeybindPacket implements IMessage
{
	
	private int p;
	public boolean pressed;
	
	public SyncKeybindPacket()
	{
	}
	
	public SyncKeybindPacket(boolean pressed)
	{
		this.p = pressed ? 1 : 0;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.p = ByteBufUtils.readVarInt(buf, 4);
		this.pressed = p == 1 ? true : false;
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		ByteBufUtils.writeVarInt(buf, p, 4);
	}

}
