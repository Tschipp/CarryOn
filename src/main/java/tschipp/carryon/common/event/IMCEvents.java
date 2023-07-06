package tschipp.carryon.common.event;

import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.InterModComms.IMCMessage;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import tschipp.carryon.CarryOn;
import tschipp.carryon.common.handler.ListHandler;
import tschipp.carryon.common.handler.ModelOverridesHandler;

import java.util.stream.Stream;

@EventBusSubscriber(modid = CarryOn.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class IMCEvents
{

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void serverLoad(InterModProcessEvent event)
	{
		Stream<IMCMessage> messages = InterModComms.getMessages(CarryOn.MODID);

		messages.forEach(msg -> {

			ListHandler.IMCMessages.add(() -> {
				String method = msg.method();
				Object obj = msg.messageSupplier().get();

				if (!(obj instanceof String str))
					return;

				switch (method) {
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
		});

	}

}
