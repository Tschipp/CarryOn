package tschipp.carryon.common.event;

import java.util.stream.Stream;

import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.InterModComms.IMCMessage;
import net.minecraftforge.fmlserverevents.FMLServerStartingEvent;
import tschipp.carryon.CarryOn;
import tschipp.carryon.common.handler.ListHandler;
import tschipp.carryon.common.handler.ModelOverridesHandler;

public class IMCEvents
{

	@SubscribeEvent(priority = EventPriority.LOW)
	public void serverLoad(FMLServerStartingEvent event)
	{
		Stream<IMCMessage> messages = InterModComms.getMessages(CarryOn.MODID);

		messages.forEach((msg) -> {

			String method = msg.method();
			Object obj = msg.messageSupplier().get();

			if(!(obj instanceof String))
				return;
			
			String str = (String)obj;
			
			switch (method)
			{
			case "blacklistBlock":
				ListHandler.FORBIDDEN_TILES.add(str);
				break;
			case "blacklistEntity":
				ListHandler.FORBIDDEN_ENTITIES.add(str);
				break;
			case "whitelistBlock":
				ListHandler.ALLOWED_TILES.add(str);
				break;
			case "whitelistEntity":
				ListHandler.ALLOWED_ENTITIES.add(str);
				break;
			case "blacklistStacking":
				ListHandler.FORBIDDEN_STACKING.add(str);
				break;
			case "whitelistStacking":
				ListHandler.ALLOWED_STACKING.add(str);
				break;
			case "addModelOverride":
				ModelOverridesHandler.parseOverride(str, 0);
				break;
			}

		});

	}
	
}
