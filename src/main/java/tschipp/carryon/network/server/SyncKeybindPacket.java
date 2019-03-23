package tschipp.carryon.network.server;

import java.util.function.Supplier;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.network.NetworkEvent;
import tschipp.carryon.client.keybinds.CarryOnKeybinds;

public class SyncKeybindPacket
{
	public boolean pressed;

	public SyncKeybindPacket(ByteBuf buf)
	{
		this.pressed = buf.readBoolean();
	}

	public SyncKeybindPacket(boolean pressed)
	{
		this.pressed = pressed;
	}

	public void toBytes(ByteBuf buf)
	{
		buf.writeBoolean(pressed);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() -> {
			
			EntityPlayerMP player = ctx.get().getSender();

			CarryOnKeybinds.setKeyPressed(player, pressed);
			
			ctx.get().setPacketHandled(true);
		});
	}

}
