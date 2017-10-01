package tschipp.carryon.network.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import tschipp.carryon.client.keybinds.CarryOnKeybinds;
import tschipp.carryon.common.scripting.ScriptChecker;

public class CarrySlotPacketHandler implements IMessageHandler<CarrySlotPacket, IMessage>
{

	@Override
	public IMessage onMessage(final CarrySlotPacket message, final MessageContext ctx)
	{
		IThreadListener mainThread = Minecraft.getMinecraft();

		mainThread.addScheduledTask(new Runnable()
		{
			EntityPlayerSP player = Minecraft.getMinecraft().player;

			@Override
			public void run()
			{
				if (message.slot >= 9)
				{
					player.getEntityData().removeTag("carrySlot");
					player.getEntityData().removeTag("overrideKey");
				}
				else
				{
					player.getEntityData().setInteger("carrySlot", message.slot);
					if(message.carryOverride != 0)
						ScriptChecker.setCarryOnOverride(player, message.carryOverride);
				}
				
				
				
			}

		});

		return null;
	}

}
