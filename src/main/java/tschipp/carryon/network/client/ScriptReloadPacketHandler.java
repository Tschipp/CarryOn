package tschipp.carryon.network.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import tschipp.carryon.common.scripting.ScriptReader;

public class ScriptReloadPacketHandler implements IMessageHandler<ScriptReloadPacket, IMessage>
{

	@Override
	public IMessage onMessage(ScriptReloadPacket message, MessageContext ctx)
	{
		IThreadListener mainThread = Minecraft.getMinecraft();

		mainThread.addScheduledTask(new Runnable()
		{
			EntityPlayerSP player = Minecraft.getMinecraft().player;

			@Override
			public void run()
			{
				if(player != null)
					ScriptReader.reloadScripts();
			}
			
		});
		
		return null;
	}

}
