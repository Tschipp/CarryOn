package tschipp.carryon.network.client;

import java.util.function.Supplier;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.network.NetworkEvent;
import tschipp.carryon.CarryOn;
import tschipp.carryon.common.scripting.ScriptReader;

public class ScriptReloadPacket
{

	public ScriptReloadPacket()
	{
	}
	
	public ScriptReloadPacket(ByteBuf buf)
	{
	}

	public void toBytes(ByteBuf buf)
	{
	}

	public void handle(Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() -> {

			EntityPlayer player = CarryOn.proxy.getPlayer();

			if (player != null)
				ScriptReader.reloadScripts();
			
			ctx.get().setPacketHandled(true);
		});

	}
}
